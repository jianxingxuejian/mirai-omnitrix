package org.hff.miraiomnitrix.listener

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.ListenerHost
import net.mamoe.mirai.event.events.MessageEvent
import org.hff.miraiomnitrix.command.CommandManager

object AnyListener : ListenerHost {

//    override fun handleException(context: CoroutineContext, exception: Throwable) {
//        println(exception.message)
//    }

    @EventHandler
    suspend fun MessageEvent.onMessage() {
        val (msg, message) = CommandManager.executeAnyCommand(sender, message, subject) ?: return
        if (msg != null) {
            subject.sendMessage(msg)
        }
        if (message != null) {
            subject.sendMessage(message)
        }
    }
}