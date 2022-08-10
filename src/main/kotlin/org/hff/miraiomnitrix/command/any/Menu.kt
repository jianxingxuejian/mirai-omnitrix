package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.buildMessageChain

object Menu : AnyCommand {

    override val name = arrayOf("menu", "help", "菜单", "帮助")

    override fun execute(sender: User, message: MessageChain, subject: Contact, args: List<String>): MessageChain {
        val result = buildMessageChain {
            +"帮助说明：\n"
            +"/bz - 查询某个人的信息\n"
        }
        return result
    }

}