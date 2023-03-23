package org.hff.miraiomnitrix.command.group

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Message
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.GroupCommand
import org.hff.miraiomnitrix.db.service.BankServer

@Command(name = ["coin", "幻书币"])
class Coin(private val bankServer: BankServer) : GroupCommand {

    init {
        val list = bankServer.list()
        if (list.isNotEmpty()) {

        }
    }

    override suspend fun execute(args: List<String>, event: GroupMessageEvent): Message? {
        TODO("Not yet implemented")
    }

}
