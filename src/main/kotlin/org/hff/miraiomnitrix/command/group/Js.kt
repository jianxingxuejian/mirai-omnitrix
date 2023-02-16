package org.hff.miraiomnitrix.command.group

import delight.nashornsandbox.NashornSandboxes
import delight.nashornsandbox.exceptions.ScriptCPUAbuseException
import net.mamoe.mirai.event.events.GroupMessageEvent
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.CommandResult
import org.hff.miraiomnitrix.command.CommandResult.Companion.result
import org.hff.miraiomnitrix.command.GroupCommand
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror
import java.util.concurrent.Executors
import javax.script.ScriptException


@Command(name = ["js"])
class Js : GroupCommand {

    private final val sandbox = NashornSandboxes.create()

    init {
        sandbox.setMaxCPUTime(100)
        sandbox.setMaxMemory(1024 * 1024)
        sandbox.allowNoBraces(false)
        sandbox.setMaxPreparedStatements(30)
        sandbox.executor = Executors.newSingleThreadExecutor()
    }

    override suspend fun execute(args: List<String>, event: GroupMessageEvent): CommandResult? {
        val command = args.joinToString(" ")
        return try {
            val result = sandbox.eval(command) ?: return result("null")
            if (result is ScriptObjectMirror) {
                if (result.isArray) {
                    if (result.size == 0) {
                        return result("[]")
                    }
                    return result(result.values.joinToString("\n"))
                }
            }
            val text = result.toString()
            if (text.isBlank()) return result("blank")
            result(text)
        } catch (error: ScriptCPUAbuseException) {
            error.message?.let { result(it) }
        } catch (error: ScriptException) {
            error.message?.let { result(it) }
        }
    }
}