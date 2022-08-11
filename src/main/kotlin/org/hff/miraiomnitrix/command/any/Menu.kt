package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.buildMessageChain
import org.hff.miraiomnitrix.command.Command

@Command(name = ["menu", "help", "菜单", "帮助"])
class Menu : AnyCommand {

    override suspend fun execute(sender: User, message: MessageChain, subject: Contact, args: List<String>): MessageChain {
        val result = buildMessageChain {
            +"帮助说明：\n"
        }
        return result
    }

}