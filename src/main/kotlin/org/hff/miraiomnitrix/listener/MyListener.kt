package org.hff.miraiomnitrix.listener

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import org.hff.miraiomnitrix.command.core.CommandManager
import kotlin.coroutines.CoroutineContext

object MyListener : SimpleListenerHost() {

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        exception.printStackTrace()
    }

    @EventHandler
    suspend fun GroupMessageEvent.onMessage() {
        CommandManager.executeGroupCommand(sender, message, group)
    }

    @EventHandler
    suspend fun FriendMessageEvent.onMessage(){
        CommandManager.executeFriendCommand(sender, message)
    }
}