package org.hff.miraiomnitrix.command.impl.group

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.command.core.Command
import org.hff.miraiomnitrix.command.type.GroupCommand
import org.hff.miraiomnitrix.result.ResultMessage
import org.hff.miraiomnitrix.result.result

@Command(name = ["复读", "说", "echo"])
class Echo : GroupCommand {
    override suspend fun execute(
        sender: Member,
        message: MessageChain,
        group: Group,
        args: List<String>
    ): ResultMessage? {
        if (args.isEmpty()) return null

        var first = args[0]
        if (first.startsWith("我是")) {
            first = first.replaceFirst("我", sender.nick)
        }
        if (args.size == 1) return result(first)

        return result(first + " " + args.slice(1 until args.size).joinToString(" "))
    }
}