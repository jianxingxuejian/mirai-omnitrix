package org.hff.miraiomnitrix.listener

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent
import org.hff.miraiomnitrix.command.core.CommandManager
import org.hff.miraiomnitrix.event.Repeat.breakRepeat
import java.util.*
import kotlin.coroutines.CoroutineContext

object GroupListener : SimpleListenerHost() {

    val groupMsgMap = mutableMapOf<Long, Queue<String>>()

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        exception.printStackTrace()
    }

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