package org.hff.miraiomnitrix.command.any

import delight.nashornsandbox.NashornSandboxes
import delight.nashornsandbox.exceptions.ScriptCPUAbuseException
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror
import java.util.concurrent.Executors
import javax.script.ScriptException


@Command(name = ["js"])
class Js : AnyCommand {

    private final val sandbox = NashornSandboxes.create()

    init {
        sandbox.setMaxCPUTime(100)
        sandbox.setMaxMemory(1024 * 1024)
        sandbox.allowNoBraces(false)
        sandbox.setMaxPreparedStatements(30)
        sandbox.executor = Executors.newSingleThreadExecutor()
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
            val text = result.toString()
            if (text.isBlank()) return "blank".toPlainText()
            text.toPlainText()
        } catch (error: ScriptCPUAbuseException) {
            error.message?.toPlainText()
        } catch (error: ScriptException) {
            error.message?.toPlainText()
        }
    }
}
