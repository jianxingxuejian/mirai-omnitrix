package org.hff.miraiomnitrix.listener

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.ListenerHost
import net.mamoe.mirai.event.events.FriendMessageEvent
import org.hff.miraiomnitrix.command.CommandManager

object FriendListener: ListenerHost {

    @EventHandler
    suspend fun FriendMessageEvent.onMessage() {
        val messageChain = CommandManager.executeFriendCommand(sender, message) ?: return
        sender.sendMessage(messageChain)
    }

}