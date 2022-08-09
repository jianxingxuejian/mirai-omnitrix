package org.hff.miraiomnitrix.command.friend

import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.command.Command

/** 适用于好友消息指令接口 */
interface FriendCommand : Command {

    /**
     * 执行指令
     *
     * @param sender 发送者
     * @param message 消息
     * @param args 追加参数
     */
    fun execute(sender: Friend, message: MessageChain, args: List<String>): MessageChain?
}