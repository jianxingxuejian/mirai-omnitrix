package org.hff.miraiomnitrix.event.group

import com.google.common.cache.CacheBuilder
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import org.hff.miraiomnitrix.common.BooleanEnum
import org.hff.miraiomnitrix.db.entity.Niuzi
import org.hff.miraiomnitrix.db.service.NiuziService
import org.hff.miraiomnitrix.event.*
import org.hff.miraiomnitrix.utils.getQq
import org.hff.miraiomnitrix.utils.toDuration
import org.springframework.boot.CommandLineRunner
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.random.Random

val fencerCache = hashMapOf<Long, HashMap<Long, Fencer>>()
val queueCache = CacheBuilder
    .newBuilder()
    .expireAfterWrite(5, TimeUnit.MINUTES)
    .build<Long, HashSet<Long>>()

private fun addQueue(groupId: Long, qq: Long) = queueCache.getIfPresent(groupId)?.add(qq)
private fun removeQueue(groupId: Long, qq: Long) = queueCache.getIfPresent(groupId)?.remove(qq)

data class Fencer(
    var niuzi: Niuzi,
    var winCount: Int = 0,
    var cooldown: LocalTime? = null,
    var safe: LocalTime? = null,
)

@EnableScheduling
@Event(priority = 2)
class Fencing(private val niuziService: NiuziService) : GroupEvent, CommandLineRunner {

    override fun run(vararg args: String?) {
        niuziService.list().groupBy { it.groupId }.forEach { (groupId, niuziList) ->
            val group = fencerCache.getOrPut(groupId) { hashMapOf() }
            niuziList.sortedByDescending { it.length }.forEach { niuzi ->
                group[niuzi.qq] = Fencer(niuzi)
            }
        }
    }

    override suspend fun GroupMessageEvent.handle(args: List<String>, isAt: Boolean): EventResult {
        if (args.isEmpty()) return stop()
        val groupCache = fencerCache[group.id] ?: return stop()
        val first = args.first()
        val last = args.last()

        if (first == "偷袭" || last == "偷袭") {
            val ambush = groupCache[sender.id] ?: return stop()
            val qq = args.getQq() ?: return stop()
            if (qq == sender.id) return stop()
            val victim = groupCache[qq] ?: return stop()

            val cooldown = ambush.cooldown
            if (cooldown != null && cooldown.isAfter(LocalTime.now())) return stop(At(sender) + "你的偷袭技能冷却中，倒计时：${cooldown.toDuration()}")
            val safe = victim.safe
            if (safe != null && safe.isAfter(LocalTime.now())) return stop(At(sender) + "对方处于保护期无法偷袭，倒计时：${safe.toDuration()}")

            val victimName = group.members[qq]?.nameCardOrNick ?: return stop()
            val ambushName = sender.nameCardOrNick

            val randomA = ambush.random(3.0)
            val randomB = victim.random(1.0)

            return buildMessageChain {
                +"🤺🤺🤺战报：\n"
                +"${ambushName}卑鄙无耻地对${victimName}发起了偷袭\n"
                val time = LocalTime.now().plusHours(1)
                ambush.cooldown = time
                if (randomA > randomB) {
                    +"偷袭成功，${victimName}的牛子猝不及防被打败了\n"
                    fight(ambush, ambushName, victim, victimName)
                    victim.safe = time
                } else {
                    +"不料${victimName}早已察觉，成功反杀${ambushName}\n"
                    fight(victim, victimName, ambush, ambushName)
                }
            }.let { stop(it) }

        }

        if (last.endsWith("击剑") || last.endsWith("🤺")) {
            val queue = queueCache.get(group.id) { hashSetOf() }
            if (queue.isEmpty()) {
                queue.add(sender.id)
                return stop()
            }
            if (queue.contains(sender.id)) return stop()
            val fencer1 = groupCache[sender.id] ?: return stop()
            val fencer1Name = sender.nameCardOrNick
            val qq = queue.random()
            val fencer2 = groupCache[qq] ?: return stop()
            val fencer2Name = group.members[qq]?.nameCardOrNick ?: return stop()

            val randomA = fencer1.random(1.0)
            val randomB = fencer2.random(1.0)

            return buildMessageChain {
                +"🤺🤺🤺战报：\n"
                +"${fencer1Name}与${fencer2Name}使用牛子进行决斗\n"
                if ((randomA - randomB).absoluteValue < 1) {
                    if (Random.nextBoolean()) {
                        +"双方缠斗不止，势均力敌，最后两方皆败\n"
                        +fencer1.niuzi.decrease(fencer1Name)
                        +fencer2.niuzi.decrease(fencer2Name)
                    } else {
                        +"双方经过血与火的磨练，最后平局\n"
                        +fencer1.niuzi.increase(fencer1Name)
                        +fencer2.niuzi.increase(fencer2Name)
                    }
                } else if (randomA > randomB) {
                    fight(fencer1, fencer1Name, fencer2, fencer2Name)
                } else {
                    fight(fencer2, fencer2Name, fencer1, fencer1Name)
                }
            }.let { stop(it) }
        }

        return next()
    }

    private fun Niuzi.decrease(name: String, value: Double? = null): Message {
        val decrease = value ?: Random.nextDouble(length * 0.05)
        length -= decrease
        removeQueue(groupId, qq)
        return if (length < 5) {
            niuziService.removeById(this)
            fencerCache[groupId]?.remove(qq)
            At(qq) + "的牛子受到重创抢救失败，请重新领养一只\n"
        } else {
            niuziService.updateById(this)
            "${name}的牛子缩短了%.2f厘米\n".format(decrease).toPlainText()
        }
    }


    private fun Niuzi.increase(name: String, value: Double? = null): Message {
        val increase = value ?: Random.nextDouble(length * 0.05)
        length += increase
        niuziService.updateById(this)
        addQueue(groupId, qq)
        return "${name}的牛子增长了%.2f厘米\n".format(increase).toPlainText()
    }

    private fun MessageChainBuilder.fight(winner: Fencer, winnerName: String, loser: Fencer, loserName: String) {
        val winnerLength = winner.niuzi.length
        val loserLength = loser.niuzi.length
        val rate = if (loserLength > winnerLength) (loserLength / winnerLength).pow(0.5) else 1.0
        val length = Random.nextDouble(loserLength * rate * 0.1)
        val text = if (winnerLength < loserLength) "以弱胜强" else "以雷霆之势"
        +"${winnerName}${text}击败了${loserName}，牛子在战斗中变强，增长了%.2f厘米\n".format(length)
        with(winner) {
            niuzi.length += length
            niuziService.updateById(niuzi)
            winCount++
            addQueue(niuzi.groupId, niuzi.qq)

        }
        with(loser) {
            niuzi.length -= length
            winCount = 0
            +niuzi.decrease(loserName, length)
        }
    }

    private fun Fencer.random(initRate: Double): Double {
        val rateA = if (winCount >= 9) 0.01 else (1 - (winCount + 1) * (winCount + 1) / 100.0)
        val random = Random.nextDouble(0.0, niuzi.length * initRate * rateA)
        println("用户--${niuzi.qq}，总长度--${niuzi.length}，roll点--$random")
        return random
    }

    @Scheduled(cron = "0 0 6 * * ?")
    fun listen() {
        val list = niuziService.list()
        list.forEach {
            it.length += Random.nextDouble(0.0, 1.0)
            it.isDay = BooleanEnum.FALSE
        }
        niuziService.updateBatchById(list)
    }

}
