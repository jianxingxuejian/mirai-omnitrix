package org.hff.miraiomnitrix.event

import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import org.springframework.stereotype.Component

@Component
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Event(
    /** 优先级 */
    val priority: Int
)

/** 全部消息事件 */
interface AnyEvent {
    suspend fun handle(args: List<String>, event: MessageEvent): EventResult
}

/** 群消息事件 */
interface GroupEvent {
    suspend fun handle(args: List<String>, event: GroupMessageEvent): EventResult
}

/** 好友消息事件 */
interface FriendEvent {
    suspend fun handle(args: List<String>, event: FriendMessageEvent): EventResult
}

/** 事件处理返回结果 stop()方法代表任务链中止，next()方法代表任务链继续 */
data class EventResult(val stop: Boolean, val msg: String?, val msgChain: MessageChain?) {
    companion object {
        fun stop() = result(true)
        fun stop(msg: String?) = result(true, msg)
        fun stop(msgChain: MessageChain?) = result(true, msgChain)
        fun next() = result(false)
        fun next(msg: String?) = result(false, msg)
        fun next(msgChain: MessageChain?) = result(false, msgChain)
        fun result(stop: Boolean) = EventResult(stop, null, null)
        fun result(stop: Boolean, msg: String?) = EventResult(stop, msg, null)
        fun result(stop: Boolean, msgChain: MessageChain?) = EventResult(stop, null, msgChain)
    }
}
