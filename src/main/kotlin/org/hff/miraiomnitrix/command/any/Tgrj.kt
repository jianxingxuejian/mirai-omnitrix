package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.CommandManager.ttsCommand
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.JsonUtil

@Command(name = ["舔狗日记", "舔狗"])
class Tgrj : AnyCommand {

    override suspend fun MessageEvent.execute(args: List<String>): Message? {
        val json = HttpUtil.getString("https://api.lilu.org.cn/shushan/huaying/random/tgrj")
        val text = JsonUtil.getStr(json, "data")
        return if (args.getOrNull(0) == "tts") {
            ttsCommand.generate(text, subject)
        } else {
            text.toPlainText()
        }
    }
}
