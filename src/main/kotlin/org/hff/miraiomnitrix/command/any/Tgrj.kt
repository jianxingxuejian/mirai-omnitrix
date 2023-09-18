package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.CommandManager.ttsCommand
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.JsonUtil

@Command(name = ["舔狗日记", "舔狗", "tgrj"])
class Tgrj : AnyCommand {

    private val apiList = mutableListOf(::api1, ::api2, ::api3)

    override suspend fun MessageEvent.execute(args: List<String>): Message? {
        val getText = apiList.randomOrNull() ?: return "暂无可用接口".toPlainText()
        return try {
            val text = getText()
            if (args.firstOrNull() == "tts") {
                ttsCommand.generate(text, subject)
            } else {
                text.toPlainText()
            }
        } catch (e: Exception) {
            apiList.remove(getText)
            "api接口报错".toPlainText()
        }
    }

    private suspend fun api1() =
        HttpUtil.getString("https://v.api.aa1.cn/api/tiangou").trim()

    private suspend fun api2(): String {
        val json = HttpUtil.getString("https://v.api.aa1.cn/api/tiangou/")
        return JsonUtil.getStr(json, "data")
    }

    private suspend fun api3() =
        HttpUtil.getString("https://api.oick.cn/dog/api.php").trim()

    private fun String.trim() = replace("\n", "")
        .replace("<p>", "")
        .replace("</p>", "")

}
