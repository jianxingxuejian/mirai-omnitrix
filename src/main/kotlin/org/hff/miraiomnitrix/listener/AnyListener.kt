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
        val messageChain = CommandManager.executeAnyCommand(sender, message, subject)
        if (messageChain != null) {
            subject.sendMessage(messageChain)
        }
    }
}