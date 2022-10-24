package org.hff.miraiomnitrix.command.group

import delight.nashornsandbox.NashornSandboxes
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.result.ResultMessage
import org.hff.miraiomnitrix.result.fail
import org.hff.miraiomnitrix.result.result


@Command(name = ["js"])
class JsCommand : GroupCommand {

    final var sandbox = NashornSandboxes.create()

    init {
        sandbox.setMaxCPUTime(1000)
        sandbox.setMaxMemory(50 * 1024)
        sandbox.setMaxPreparedStatements(30)
    }

    override suspend fun execute(
        sender: Member,
        message: MessageChain,
        group: Group,
        args: List<String>
    ): ResultMessage? {
        val command = args.joinToString(" ")
        try {
            val result = sandbox.eval(command)
            println(result)
        } catch (error: Exception) {
            result("执行错误")
        }
        return null
    }
}