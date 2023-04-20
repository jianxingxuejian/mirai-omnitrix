package org.hff.miraiomnitrix.event.group

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.db.service.BankService
import org.hff.miraiomnitrix.event.Event
import org.hff.miraiomnitrix.event.EventResult
import org.hff.miraiomnitrix.event.GroupEvent
import org.hff.miraiomnitrix.event.stop
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import kotlin.math.pow
import kotlin.random.Random

@EnableScheduling
@Event(priority = 6)
class Mine(private val bankService: BankService, private val permissionProperties: PermissionProperties) : GroupEvent {

    private val cache = hashMapOf<Long, Mine>()
    private val limit = Int.MAX_VALUE * 15L

    override suspend fun GroupMessageEvent.handle(args: List<String>, isAt: Boolean): EventResult {
        if (!permissionProperties.mineIncludeGroup.contains(subject.id)) return stop()
        val hashCode = message.hashCode()
        if (hashCode <= 0) return stop()

        val qq = sender.id
        cache.getOrPut(qq) { Mine(0, 0) }.run {
            cumulative += hashCode
            if (cumulative >= 2.0.pow(coin) * limit) {
                cumulative = 0
                return if (Random.nextBoolean()) {
                    bankService.getByQq(qq).apply { coin += 1 }.let(bankService::updateById)
                    coin += 1
                    stop(message.quote() + "给你一枚幻书币~")
                } else {
                    stop(At(sender) + "您好。今天您的发言频率过多，已失去本群发言权利。想要继续发言，请下载原神客户端游玩，官方网址：game.granbluefantasy.jp")
                }

            }
        }

        return stop()
    }

    data class Mine(var coin: Int, var cumulative: Long)

    @Scheduled(cron = "0 0 6 * * ?")
    fun listen() {
        cache.clear()
    }

}
