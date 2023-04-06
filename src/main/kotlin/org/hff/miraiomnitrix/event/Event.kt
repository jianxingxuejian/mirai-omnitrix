package org.hff.miraiomnitrix.event

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.events.UserMessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.common.Allow
import org.springframework.stereotype.Component

/**
 * 事件注解
 *
 * @property priority 优先级
 */
@Component
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Event(val priority: Int)

/** 全部消息事件,可能来自群或者用户 */
interface AnyEvent : Handler<MessageEvent>, Allow

/** 群消息事件 */
interface GroupEvent : Handler<GroupMessageEvent>

/** 用户消息事件 */
interface UserEvent : Handler<UserMessageEvent>, Allow

/** 事件执行接口 */
sealed interface Handler<T : MessageEvent> {
    suspend fun T.handle(args: List<String>, isAt: Boolean): EventResult
}

/** 事件处理返回结果 stop()方法代表任务链中止，next()方法代表任务链继续 */
data class EventResult(val stop: Boolean, val message: Message?)

fun stop() = result(true)
fun stop(msg: String) = result(true, msg.toPlainText())
fun stop(message: Message) = result(true, message)
fun next() = result(false)
fun next(msg: String) = result(false, msg.toPlainText())
fun next(message: Message) = result(false, message)
fun result(stop: Boolean) = EventResult(stop, null)
fun result(stop: Boolean, message: Message) = EventResult(stop, message)
