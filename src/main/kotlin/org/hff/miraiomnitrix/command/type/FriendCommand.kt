package org.hff.miraiomnitrix.command.type

import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.result.CommandResult

/** 适用于好友消息指令接口 */
interface FriendCommand {

    /**
     * 执行指令
     *
     * @param sender 发送者
     * @param message 消息
     * @param args 追加参数
     * @param event 好友消息事件
     */
    suspend fun execute(sender: Friend, message: MessageChain, args: List<String>, event: FriendMessageEvent): CommandResult?

}