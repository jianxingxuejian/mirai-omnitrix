package org.hff.miraiomnitrix.command.group

import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.GroupCommand
import org.hff.miraiomnitrix.common.BooleanEnum
import org.hff.miraiomnitrix.db.entity.Niuzi
import org.hff.miraiomnitrix.db.service.BankService
import org.hff.miraiomnitrix.db.service.NiuziService
import org.hff.miraiomnitrix.event.group.Fencer
import org.hff.miraiomnitrix.event.group.fencerCache
import org.hff.miraiomnitrix.utils.toDuration
import kotlin.random.Random

@Command(name = ["niu", "niuzi", "牛子"])
class Niuzi(private val niuziService: NiuziService, private val bankService: BankService) : GroupCommand {

    val help = """
        |使用领养、adopt二级命令来领养一只牛子
        |使用排名、rank、top二级命令查看排名
        |当拥有牛子的人使用击剑的文字或者emoji开头，5分钟内其他人可以用相同方式开始一轮挑战
        |每1小时可以偷袭其他人，胜率三倍，失败惩罚双倍
    """.trimMargin().toPlainText()

    val shop = """
        |1. 大保健，牛子随机增长0~1厘米，售价500金币，限定一天一次。
        |2，辟邪剑法残页，购买后会暂时领悟辟邪剑法的奥义，杀死牛子，售价100金币。
        |3，偷袭冷却券，清空当前偷袭冷却，售价1000金币。
        |
        |使用`.niu buy/买 序号`的方式来购买商品
        |每日可通过`.coin 签到`来赚取金币
    """.trimMargin().toPlainText()

    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        return when (args.getOrNull(0)) {
            null -> help
            "help", "帮助" -> help
            "top", "rank", "排行榜" -> {
                val cache = fencerCache[group.id] ?: return "排行榜为空".toPlainText()
                cache.values.sortedByDescending { it.niuzi.length }.take(10).mapIndexed { index, fencer ->
                    with(fencer) {
                        val winCount = if (winCount != 0) "，连胜${winCount}场" else ""
                        val safe = if (safe != null) "，保护期剩余${safe!!.toDuration()}" else ""
                        "第${index + 1}名，${group.members[niuzi.qq]?.nameCardOrNick}：%.2f厘米$winCount$safe".format(
                            niuzi.length
                        )
                    }
                }.joinToString("\n").toPlainText()
            }

            "领养", "adopt" -> {
                val cache = fencerCache.getOrPut(group.id) { hashMapOf() }
                val niuzi = cache[sender.id]
                if (niuzi != null) return "你已经领养了一只牛子".toPlainText()
                val random = Random.nextDouble(5.0, 10.0)
                val newNiuzi = Niuzi(null, group.id, sender.id, random, BooleanEnum.FALSE)
                val save = niuziService.save(newNiuzi)
                if (!save) return "领养失败".toPlainText()
                cache[sender.id] = Fencer(newNiuzi)
                "你领养了一只牛子，长度：${random}厘米".toPlainText()
            }

            "商店", "shop" -> shop
            "买", "buy" -> {
                val no = args.getOrNull(1)?.toIntOrNull() ?: return At(sender) + "参数错误"
                if (no < 1 || no > 3) return At(sender) + "商品序号错误"
                val bank = bankService.getByQq(sender.id)
                val fencer = fencerCache[group.id]?.get(sender.id) ?: return "你没有牛子".toPlainText()
                when (no) {
                    1 -> with(fencer) {
                        if (bank.money < 500) return At(sender) + "金币不足"
                        if (niuzi.isDay == BooleanEnum.TRUE) return At(sender) + "今天已经做过大保健了，请明天再来"
                        bank.money -= 500
                        bankService.updateById(bank)
                        val random = Random.nextDouble(0.0, 1.0)
                        niuzi.isDay = BooleanEnum.TRUE
                        niuzi.length += random
                        niuziService.updateById(niuzi)
                        At(sender) + "做完大保健，牛子增长了${random}厘米"
                    }

                    2 -> {
                        if (bank.money < 100) return At(sender) + "金币不足"
                        bank.money -= 100
                        bankService.updateById(bank)
                        niuziService.removeById(fencer.niuzi)
                        fencerCache[group.id]?.remove(sender.id)
                        At(sender) + "你的牛子永远离开了你"
                    }

                    3 -> {
                        if (bank.money < 1000) return At(sender) + "金币不足"
                        bank.money -= 1000
                        bankService.updateById(bank)
                        fencer.cooldown = null
                        At(sender) + "冷却清空成功"
                    }

                    else -> null
                }
            }

            else -> help
        }
    }

}
