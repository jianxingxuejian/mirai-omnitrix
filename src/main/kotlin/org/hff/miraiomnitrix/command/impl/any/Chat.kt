package org.hff.miraiomnitrix.command.impl.any

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.command.core.Command
import org.hff.miraiomnitrix.command.type.AnyCommand
import org.hff.miraiomnitrix.result.ResultMessage
import org.hff.miraiomnitrix.result.result

@Command(["chat", "聊天"])
class Chat : AnyCommand {

    private val API = "https://api.openai.com/v1/"
    override suspend fun execute(
        sender: User,
        message: MessageChain,
        subject: Contact,
        args: List<String>
    ): ResultMessage? {
        if (args.isEmpty()) {
            return result("使用openai开始聊天，通过回复机器人来衔接上下文")
        }
        return null
    }
}