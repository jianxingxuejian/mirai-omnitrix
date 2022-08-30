package org.hff.miraiomnitrix.listener

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.ListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent
import org.hff.miraiomnitrix.command.CommandManager

object GroupListener : ListenerHost {

    @EventHandler
    suspend fun GroupMessageEvent.onMessage() {
        val (msg, message) = CommandManager.executeGroupCommand(sender, message, group) ?: return
        if (msg != null) {
            subject.sendMessage(msg)
        }
        if (message != null) {
            subject.sendMessage(message)
        }
    }

}