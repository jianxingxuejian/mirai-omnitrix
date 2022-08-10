package org.hff.miraiomnitrix.listener

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.ListenerHost
import net.mamoe.mirai.event.events.MessageEvent
import org.hff.miraiomnitrix.command.CommandManager

object AnyListener : ListenerHost {

    @EventHandler
    suspend fun MessageEvent.onMessage() {
        val messageChain = CommandManager.executeAnyCommand(sender, message, subject) ?: return
        subject.sendMessage(messageChain)
    }
}