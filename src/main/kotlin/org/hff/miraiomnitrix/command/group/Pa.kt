package org.hff.miraiomnitrix.command.group

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.buildMessageChain

object Pa : GroupCommand {

    override val name = arrayOf("爬")
    override val isNeedHeader = false

    override fun execute(sender: Member, message: MessageChain, group: Group, args: List<String>): MessageChain {
        val result =  buildMessageChain {
            +"爬：\n"
        }
        return result
    }


}