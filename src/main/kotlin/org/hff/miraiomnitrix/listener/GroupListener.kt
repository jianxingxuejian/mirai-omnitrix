package org.hff.miraiomnitrix.listener

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.ListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent
import org.hff.miraiomnitrix.command.CommandManager
import org.hff.miraiomnitrix.event.Repeat.breakRepeat
import java.util.*

object GroupListener : ListenerHost {

    val groupMsgMap = mutableMapOf<Long, Queue<String>>()

    @EventHandler
    suspend fun GroupMessageEvent.onMessage() {
        if (!arrayOf(864852990L).contains(group.id)) {
            breakRepeat(group, message)
        }

        val (msg, message) = CommandManager.executeGroupCommand(sender, message, group) ?: return
        if (msg != null) {
            group.sendMessage(msg)
        }
        if (message != null) {
            group.sendMessage(message)
        }
    }

}