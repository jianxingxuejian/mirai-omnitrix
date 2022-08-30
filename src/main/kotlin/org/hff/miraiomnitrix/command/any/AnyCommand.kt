package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.result.ResultMessage

/** 适用于所有消息指令接口 */
interface AnyCommand {

    /**
     * 执行指令
     *
     * @param sender 发送人
     * @param message 消息
     * @param subject 联系人
     * @param args 追加参数
     */
    suspend fun execute(sender: User, message: MessageChain, subject: Contact, args: List<String>): ResultMessage?

}