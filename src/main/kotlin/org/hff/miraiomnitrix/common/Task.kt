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
    @Scheduled(initialDelay = 60 * 60 * 1000, fixedDelay = 4 * 60 * 60 * 1000)
    fun nudge() {
        runBlocking {
            val group = bot.groups.random()
            val member = group.members.random()
            member.nudge().sendTo(group)
        }
    }

}
