package org.hff.miraiomnitrix.common

import kotlinx.coroutines.runBlocking
import org.hff.miraiomnitrix.bot
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@EnableScheduling
@Component
class Task {

    /** 随机戳一个群友 */
    @Scheduled(cron = "0 0 0,2,10,12,14,16,18,20,22 * * ?")
    fun nudge() {
        runBlocking {
            val groupId = messageCache.keys.randomOrNull() ?: return@runBlocking
            val group = bot.groups[groupId] ?: return@runBlocking
            val memberId = messageCache[groupId]!!
            val member = group[memberId] ?: return@runBlocking
            member.nudge().sendTo(group)
        }
    }

}
