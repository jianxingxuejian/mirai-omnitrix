package org.hff.miraiomnitrix.command.group

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.GroupCommand
import org.hff.miraiomnitrix.utils.getInfo

@Command(name = ["history", "黑历史"])
class BlackHistory : GroupCommand {

    override suspend fun execute(args: List<String>, event: GroupMessageEvent): Message? {
        if (args.isEmpty()) return "使用二级指令，list/page：列表，add/save：添加历史".toPlainText()
        val (subject, _, message) = event.getInfo()
        val quote = message[QuoteReply.Key]

        when (args[0]) {
            "list" -> {

            }

            "add", "save" -> {
                return "添加历史".toPlainText()
            }

            else -> {
                return "未知指令".toPlainText()
            }
        }
        return null
    }
}
