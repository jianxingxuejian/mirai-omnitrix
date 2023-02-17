package org.hff.miraiomnitrix.event

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.events.UserMessageEvent
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.common.Allow
import org.springframework.stereotype.Component

@Component
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Event(
    /** 优先级 */
    val priority: Int
)

/** 全部消息事件,可能来自群或者用户 */
interface AnyEvent : Handle<MessageEvent>, Allow

/** 群消息事件 */
interface GroupEvent : Handle<GroupMessageEvent>

/** 用户消息事件 */
interface UserEvent : Handle<UserMessageEvent>, Allow

/** 事件执行接口 */
sealed interface Handle<T : MessageEvent> {
    suspend fun handle(args: List<String>, event: T): EventResult
}

/** 事件处理返回结果 stop()方法代表任务链中止，next()方法代表任务链继续 */
data class EventResult(val stop: Boolean, val msg: String?, val msgChain: MessageChain?)

fun stop() = result(true)
fun stop(msg: String?) = result(true, msg)
fun stop(msgChain: MessageChain?) = result(true, msgChain)
fun next() = result(false)
fun next(msg: String?) = result(false, msg)
fun next(msgChain: MessageChain?) = result(false, msgChain)
fun result(stop: Boolean) = EventResult(stop, null, null)
fun result(stop: Boolean, msg: String?) = EventResult(stop, msg, null)
fun result(stop: Boolean, msgChain: MessageChain?) = EventResult(stop, null, msgChain)
