package org.hff.miraiomnitrix.command

import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import org.springframework.stereotype.Component

@Component
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Command(
    /** 指令名称列表 */
    val name: Array<String>
)

/** 全部消息指令 */
interface AnyCommand {
    suspend fun execute(args: List<String>, event: MessageEvent): CommandResult?
}

/** 群消息指令 */
interface GroupCommand {
    suspend fun execute(args: List<String>, event: GroupMessageEvent): CommandResult?
}

/** 好友消息指令 */
interface FriendCommand {
    suspend fun execute(args: List<String>, event: FriendMessageEvent): CommandResult?
}

/** 指令执行结果 */
data class CommandResult(val msg: String?, val msgChain: MessageChain?) {
    companion object {
        fun result(msg: String) = CommandResult(msg, null)
        fun result(message: MessageChain) = CommandResult(null, message)
    }

}