package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.common.errorCache

@Command(name = ["error"])
class Error : AnyCommand {

    override suspend fun MessageEvent.execute(args: List<String>): Message? {
        errorCache.getIfPresent(subject.id)?.run {
            if (stackTrace.isEmpty()) return "错误堆栈为空".toPlainText()
            val maxLines = (args.getOrNull(0)?.toIntOrNull() ?: 10).coerceIn(1, stackTrace.size)
            return stackTrace.take(maxLines).joinToString("\n").toPlainText()
        } ?: return "没有错误信息".toPlainText()
    }

}
