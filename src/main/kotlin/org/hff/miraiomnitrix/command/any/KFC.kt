package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.CommandManager
import org.hff.miraiomnitrix.utils.HttpUtil

@Command(name = ["kfc", "肯德基"])
class KFC : AnyCommand {

    override suspend fun execute(args: List<String>, event: MessageEvent): Message? {
        val text = HttpUtil.getStringByProxy("https://kfc-crazy-thursday.vercel.app/api/index")
        return if (args.getOrNull(0) == "tts") {
            CommandManager.getCommand<Tts>().tts(text, event.subject)
        } else {
            text.toPlainText()
        }
    }
}
