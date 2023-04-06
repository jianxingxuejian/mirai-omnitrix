package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.common.recallLastMessage

@Command(name = ["recall", "撤回"])
class Recall : AnyCommand {

    override suspend fun MessageEvent.execute(args: List<String>): Message? {
        val num = args.getOrNull(0)?.toIntOrNull() ?: return "只能输入数字".toPlainText()
        if (num < 1 || num > 10) return "只能输入1-10的数字".toPlainText()
        recallLastMessage(subject.id, num)
        return null
    }

}

