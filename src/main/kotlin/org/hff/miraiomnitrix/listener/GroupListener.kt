package org.hff.miraiomnitrix.listener

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.ListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent

object GroupListener: ListenerHost {

    @EventHandler
    suspend fun GroupMessageEvent.onMessage() {

    }
}