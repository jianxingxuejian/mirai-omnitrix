package org.hff.miraiomnitrix.command.type

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.result.ResultMessage

/** 适用于群消息指令接口 */
interface GroupCommand {


    /**
     * 执行指令
     *
     * @param sender 发送者
     * @param message 消息
     * @param group 群
     * @param args 追加参数
     */
    suspend fun execute(sender: Member, message: MessageChain, group: Group, args: List<String>): ResultMessage?

}