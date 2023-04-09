package org.hff.miraiomnitrix.command.any

import delight.nashornsandbox.NashornSandboxes
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror
import java.util.concurrent.Executors


@Command(name = ["js"])
class Js : AnyCommand {

    private val sandbox = NashornSandboxes.create().apply {
        setMaxCPUTime(100)
        setMaxMemory(1024 * 1024)
        allowNoBraces(false)
        setMaxPreparedStatements(30)
        executor = Executors.newSingleThreadExecutor()
    }

    override suspend fun MessageEvent.execute(args: List<String>): Message? {
        val command = args.joinToString(" ")
        return try {
            val result = sandbox.eval(command) ?: return "null".toPlainText()
            if (result is ScriptObjectMirror) {
                if (result.isArray) {
                    if (result.size == 0) return "[]".toPlainText()
                    return result.values.joinToString("\n").toPlainText()
                }
            }
            result.toString().apply { if (isBlank()) return "blank".toPlainText() }
        } catch (error: Exception) {
            error.message
        }?.toPlainText()
    }
}
