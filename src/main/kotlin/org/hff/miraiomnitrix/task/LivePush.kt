package org.hff.miraiomnitrix.task

import kotlinx.coroutines.runBlocking
import org.hff.miraiomnitrix.BotRunner
import org.hff.miraiomnitrix.app.service.LiveService
import org.hff.miraiomnitrix.utils.Util
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@Configuration
@EnableScheduling
class LivePush(val liveService: LiveService) {

    val livesCache = mutableMapOf<Long, Int>()

    @Scheduled(initialDelay = 60_000, fixedDelay = 90_000)
    fun listen() {
        runBlocking {
            val lives = liveService.list()
            lives.forEach {
                val cache = livesCache[it.qq]
                val userInfo = Util.getBilibiliUserInfo(it.uid)
                val liveRoom = userInfo.live_room ?: return@forEach
                if (cache != null) {
                    val group = BotRunner.bot.groups[it.groupId] ?: return@forEach
                    if (cache == 0 && liveRoom.liveStatus == 1) {
                        group.sendMessage("${userInfo.name}开始直播，地址:${liveRoom.url}")
                    }
                }
                livesCache[it.qq] = liveRoom.liveStatus
            }
        }
    }
}