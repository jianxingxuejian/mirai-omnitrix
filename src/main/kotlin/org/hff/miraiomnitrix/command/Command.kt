package org.hff.miraiomnitrix.command

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.events.UserMessageEvent
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.common.Allow
import org.springframework.stereotype.Component

/**
 * 使用该注解注册指令
 *
 * @property name 指令名称列表
 */
@Component
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Command(val name: Array<String>)

/** 全部消息指令，可能来自群或者用户 */
interface AnyCommand : Execute<MessageEvent>, Allow

/** 群指令 */
interface GroupCommand : Execute<GroupMessageEvent>

/** 用户指令 */
interface UserCommand : Execute<UserMessageEvent>, Allow

/** 指令执行接口 */
sealed interface Execute<T : MessageEvent> {
    suspend fun execute(args: List<String>, event: T): CommandResult?
}

/** 指令执行结果 */
data class CommandResult(val msg: String?, val msgChain: MessageChain?)

fun result(msg: String) = CommandResult(msg, null)
fun result(message: MessageChain) = CommandResult(null, message)