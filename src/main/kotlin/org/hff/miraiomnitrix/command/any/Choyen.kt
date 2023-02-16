package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.event.events.MessageEvent
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.CommandResult

@Command(name = ["红白", "choyen", "5000"])
class Choyen: AnyCommand {

    override suspend fun execute(args: List<String>, event: MessageEvent): CommandResult? {
        TODO("Not yet implemented")
    }


}