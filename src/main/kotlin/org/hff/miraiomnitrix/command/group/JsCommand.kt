package org.hff.miraiomnitrix.command.group

import delight.nashornsandbox.NashornSandboxes
import delight.nashornsandbox.exceptions.ScriptCPUAbuseException
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.result.ResultMessage
import org.hff.miraiomnitrix.result.result
import java.util.concurrent.Executors
import javax.script.ScriptException


@Command(name = ["js"])
class JsCommand : GroupCommand {

    private final val sandbox = NashornSandboxes.create()

    init {
        sandbox.setMaxCPUTime(100)
        sandbox.setMaxMemory(1024 * 1024)
        sandbox.allowNoBraces(false)
        sandbox.setMaxPreparedStatements(30)
        sandbox.executor = Executors.newSingleThreadExecutor()
    }

    override suspend fun execute(
        sender: Member,
        message: MessageChain,
        group: Group,
        args: List<String>
    ): ResultMessage? {
        val command = args.joinToString(" ")
        return try {
            val result = sandbox.eval(command)
            result(result.toString())
        } catch (error: ScriptCPUAbuseException) {
            error.message?.let { result(it) }
        } catch (error: ScriptException) {
            error.message?.let { result(it) }
        }
    }
}