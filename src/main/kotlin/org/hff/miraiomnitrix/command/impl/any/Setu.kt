package org.hff.miraiomnitrix.command.impl.any

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.ForwardMessageBuilder
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.toMessageChain
import org.hff.miraiomnitrix.command.core.Command
import org.hff.miraiomnitrix.command.type.AnyCommand
import org.hff.miraiomnitrix.result.ResultMessage
import org.hff.miraiomnitrix.result.result
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.JsonUtil
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern


@Command(name = ["涩图", "色图", "setu"])
class Setu : AnyCommand {

    private val url1 = "https://api.lolicon.app/setu/v2?"

    private val url2 = "https://image.anosu.top/pixiv/json?size=regular&"

    override suspend fun execute(
        sender: User,
        message: MessageChain,
        subject: Contact,
        args: List<String>
    ): ResultMessage? {
        var r18 = 0
        var num = 1
        var type = 1
        val keywords = mutableListOf<String>()
        args.forEach {
            it.lowercase()
            when {
                it == "r" -> r18 = 1
                it.startsWith("n") || it.startsWith("num") ->
                    num = Pattern.compile("[^0-9]").matcher(it).replaceAll("").toInt()

                it == "b" -> type = 2
                else -> keywords.add(it)
            }
        }
        val url = (if (type == 1) {
            val sb = StringBuilder()
            keywords.forEach { sb.append("tag=").append(URLEncoder.encode(it, StandardCharsets.UTF_8)).append("&") }
            url1 + sb
        } else {
            if (keywords.isEmpty()) url2
            else url2 + "keyword=" + keywords.joinToString("|") + "&"
        }) + "r18=" + r18 + "&num=" + num
        val json = HttpUtil.getString(url)
        val forwardBuilder = ForwardMessageBuilder(subject)

        suspend fun uploadImg(imgUrl: String) {
            try {
                val result = HttpUtil.getInputStreamByProxy(imgUrl)
                val image = subject.uploadImage(result)
                forwardBuilder.add(subject.bot, image)
            } catch (_: Exception) {
            }
        }

        coroutineScope {
            if (type == 1) {
                val data = JsonUtil.getArray(json, "data")
                data.forEach {
                    launch {
                        val imgUrl = it.asJsonObject.get("urls").asJsonObject.get("original").asString
                        uploadImg(imgUrl)
                    }
                }
            } else {
                val data = JsonUtil.getArray(json)
                data.forEach {
                    launch {
                        val imgUrl = it.asJsonObject.get("url").asString
                        uploadImg(imgUrl)
                    }
                }
            }
        }

        if (forwardBuilder.size > 0) {
            return result(forwardBuilder.build().toMessageChain())
        }
        return result("没有找到符合条件的涩图")
    }
}