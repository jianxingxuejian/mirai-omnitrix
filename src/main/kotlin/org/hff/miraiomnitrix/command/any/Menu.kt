package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.MessageChain

object Menu : AnyCommand {

    override val name = arrayOf("menu", "菜单")

    override fun execute(sender: User, message: MessageChain, contact: Contact, args: Array<String>) {
    }

}