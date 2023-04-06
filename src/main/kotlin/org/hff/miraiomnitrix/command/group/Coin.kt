package org.hff.miraiomnitrix.command.group

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Message
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.GroupCommand
import org.hff.miraiomnitrix.db.service.BankServer
import org.springframework.boot.CommandLineRunner

@Command(name = ["coin", "幻书币"])
class Coin(private val bankServer: BankServer) : GroupCommand, CommandLineRunner {

    override fun run(vararg args: String?) {
        val list = bankServer.list()
        if (list.isNotEmpty()) {

        }
    }

    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        return null
    }

}
