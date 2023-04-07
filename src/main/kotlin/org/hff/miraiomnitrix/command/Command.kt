package org.hff.miraiomnitrix.command

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.events.UserMessageEvent
import net.mamoe.mirai.message.data.Message
import org.hff.miraiomnitrix.common.Allow
import org.springframework.stereotype.Component

/**
 * 使用该注解注册指令
 *
 * @property name 指令名称列表
 * @property needHead 是否需要指令头，默认需要
 */
@Component
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Command(val name: Array<String>, val needHead: Boolean = true)

/** 全部消息指令，可能来自群或者用户 */
interface AnyCommand : Execute<MessageEvent>, Allow

/** 群指令 */
interface GroupCommand : Execute<GroupMessageEvent>

/** 用户指令 */
interface UserCommand : Execute<UserMessageEvent>, Allow

/** 指令执行接口 */
sealed interface Execute<T : MessageEvent> {
    /** 是否需要指令头 */
    var needHead: Boolean
        get() = true
        set(value) {}

    /**
     * 执行指令
     *
     * @param args 追加参数
     * @return 要发送的消息
     */
    suspend fun T.execute(args: List<String>): Message?
}
