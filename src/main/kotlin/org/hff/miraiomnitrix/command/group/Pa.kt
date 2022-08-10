package org.hff.miraiomnitrix.command.group

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.buildMessageChain
import org.hff.miraiomnitrix.command.Command

@Command(name = ["爬"], isNeedHeader = false)
class Pa : GroupCommand {

    override fun execute(sender: Member, message: MessageChain, group: Group, args: List<String>): MessageChain {
        val result = buildMessageChain {
            +"爬：\n"
        }
        return result
    }


}