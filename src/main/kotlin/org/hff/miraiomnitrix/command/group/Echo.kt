package org.hff.miraiomnitrix.command.group

import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.ForwardMessageBuilder
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.GroupCommand
import org.hff.miraiomnitrix.utils.getQq

@Command(name = ["复读", "说", "echo"])
class Echo : GroupCommand {

    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        if (args.isEmpty()) return null
        val first = args[0]
        if (first.startsWith("我是")) return first.replaceFirst("我", sender.nick).toPlainText()

        val forward = ForwardMessageBuilder(group)
        var qq: Long? = null
        args.forEach {
            when (val number = it.getQq()) {
                null -> {
                    if (qq == null) return@forEach
                    val member = group[qq!!] ?: return@forEach
                    forward.add(member.id, member.nameCardOrNick, it.toPlainText())
                }

                else -> qq = number
            }
        }

        if (forward.size > 0) return forward.build()
        return "$first ${args.drop(1).joinToString(" ")}".toPlainText()
    }
}
