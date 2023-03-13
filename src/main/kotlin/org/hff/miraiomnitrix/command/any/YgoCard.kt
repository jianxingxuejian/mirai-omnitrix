package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.event.events.MessageEvent
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.CommandResult

@Command(name = ["游戏王", "ygocard", "ygo"])
sealed class YgoCard : AnyCommand {

    override suspend fun execute(args: List<String>, event: MessageEvent): CommandResult? {
        return null
    }

}