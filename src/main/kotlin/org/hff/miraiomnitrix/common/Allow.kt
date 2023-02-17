package org.hff.miraiomnitrix.common

import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupTempMessageEvent
import net.mamoe.mirai.event.events.StrangerMessageEvent
import net.mamoe.mirai.event.events.UserMessageEvent

/** 用户指令访问控制接口 */
interface Allow {
    /** 是否允许好友使用，默认允许 */
    val allowFriend: Boolean
        get() = true

    /** 是否允许群临时会话使用，默认不允许 */
    val allowGroupTemp: Boolean
        get() = false

    /** 是否允许陌生人使用，默认不允许 */
    val allowStranger: Boolean
        get() = false
}

/** 检查是否能访问 */
fun <T : Allow> T.check(event: UserMessageEvent) = when (event) {
    is FriendMessageEvent -> allowFriend
    is GroupTempMessageEvent -> allowGroupTemp
    is StrangerMessageEvent -> allowStranger
    else -> false
}