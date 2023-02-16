package org.hff.miraiomnitrix.listen

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import org.hff.miraiomnitrix.command.CommandManager
import org.hff.miraiomnitrix.event.EventManger
import kotlin.coroutines.CoroutineContext

/** 监听群消息与好友消息，执行指令或者触发事件 */
object MyListener : SimpleListenerHost() {

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        exception.printStackTrace()
    }

    @EventHandler
    suspend fun GroupMessageEvent.onMessage() = handleMessage(this)

    @EventHandler
    suspend fun FriendMessageEvent.onMessage() = handleMessage(this)

}

suspend fun handleMessage(event: MessageEvent) {
    val (execute, args) = CommandManager.handle(event)
    if (execute) return
    EventManger.handle(args, event)
}