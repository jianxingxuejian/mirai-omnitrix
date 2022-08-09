package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.buildMessageChain
import org.hff.miraiomnitrix.command.Command

/** 适用于所有消息指令接口 */
interface AnyCommand : Command {

    /**
     * 执行指令
     *
     * @param sender 发送人
     * @param message 消息
     * @param subject 联系人
     * @param args 追加参数
     */
    fun execute(sender: User, message: MessageChain, subject: Contact, args: List<String>): MessageChain? {
        buildMessageChain {
            +"帮助说明：\n"
            +"/bz - 查询某个人的信息\n"
        }
    }
}