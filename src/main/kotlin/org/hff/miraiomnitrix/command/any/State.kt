package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.utils.toTime
import java.lang.management.ManagementFactory

@Command(name = ["state", "状态"])
class State : AnyCommand {

    override suspend fun MessageEvent.execute(args: List<String>): Message? {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val time = ManagementFactory.getRuntimeMXBean().uptime.toTime()
        return """
            |内存使用: ${totalMemory / 1024 / 1024}MB
            |已运行: $time
        """.trimMargin().toPlainText()
    }

}
