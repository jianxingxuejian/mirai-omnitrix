package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.CommandManager.ttsCommand
import org.hff.miraiomnitrix.utils.HttpUtil

@Command(name = ["kfc", "肯德基"])
class KFC : AnyCommand {

    override suspend fun MessageEvent.execute(args: List<String>): Message? {
        val text = HttpUtil.getString("https://kfc-crazy-thursday.vercel.app/api/index", isProxy = true)
        return if (args.getOrNull(0) == "tts") {
            ttsCommand.generate(text, subject)
        } else {
            text.toPlainText()
        }
    }
}
