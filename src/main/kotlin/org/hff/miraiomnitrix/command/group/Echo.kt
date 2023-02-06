package org.hff.miraiomnitrix.command.group

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.ForwardMessageBuilder
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.toMessageChain
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.result.CommandResult
import org.hff.miraiomnitrix.result.CommandResult.Companion.result

@Command(name = ["复读", "说", "echo"])
class Echo : GroupCommand {

    override suspend fun execute(
        sender: Member,
        message: MessageChain,
        group: Group,
        args: List<String>,
        event: GroupMessageEvent
    ): CommandResult? {
        if (args.isEmpty()) return null
        val first = args[0]
        if (first.startsWith("我是")) {
            return result(first.replaceFirst("我", sender.nick))
        }

        val forwardBuilder = ForwardMessageBuilder(group)
        var qq: Long? = null
        args.forEach {
            if (it.startsWith("@")) {
                qq = it.substring(1).toLong()
            } else {
                try {
                    qq = it.toLong()
                } catch (_: NumberFormatException) {
                    if (qq == null) return@forEach
                    val member = group[qq!!] ?: return@forEach
                    forwardBuilder.add(member.id, member.nameCardOrNick, PlainText(it))
                }
            }
        }
        if (forwardBuilder.size > 0) return result(forwardBuilder.build().toMessageChain())

        return result(first + " " + args.slice(1 until args.size).joinToString(" "))
    }
}