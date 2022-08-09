package org.hff.miraiomnitrix.command.group

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.MessageChain

object Pa : GroupCommand {

    override val name = arrayOf("爬")
    override val isNeedHeader = false

    override fun execute(sender: Member, message: MessageChain, group: Group, args: Array<String>) {

    }


}