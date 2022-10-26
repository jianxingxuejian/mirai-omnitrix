package org.hff.miraiomnitrix.command.group

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.result.ResultMessage
import org.hff.miraiomnitrix.result.result

@Command(name = ["复读", "说", "repeat", "echo"])
class Repeat : GroupCommand {
    override suspend fun execute(
        sender: Member,
        message: MessageChain,
        group: Group,
        args: List<String>
    ): ResultMessage? {
        if (args.isNotEmpty()) {
            if(args[0] == "我是猪"){
                return result("你是猪")
            }
            return result(args.joinToString(" "))
        }
        return null
    }
}