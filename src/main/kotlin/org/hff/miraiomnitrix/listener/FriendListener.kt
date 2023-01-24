package org.hff.miraiomnitrix.listener

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.FriendMessageEvent
import org.hff.miraiomnitrix.command.core.CommandManager
import kotlin.coroutines.CoroutineContext

object FriendListener : SimpleListenerHost() {

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        exception.printStackTrace()
    }

    @EventHandler
    suspend fun FriendMessageEvent.onMessage() {
        val (msg, message) = CommandManager.executeFriendCommand(sender, message) ?: return
        if (msg != null) {
            subject.sendMessage(msg)
        }
        if (message != null) {
            subject.sendMessage(message)
        }
    }

}