package org.hff.miraiomnitrix.command.any

import cn.hutool.json.JSONUtil
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.ForwardMessageBuilder
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.toMessageChain
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.result.ResultMessage
import org.hff.miraiomnitrix.result.fail
import org.hff.miraiomnitrix.result.result
import org.hff.miraiomnitrix.utils.HttpUtil
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

@Command(name = ["涩图", "色图", "setu"])
class Setu : AnyCommand {

    private val url = "https://api.lolicon.app/setu/v2"

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun execute(
        sender: User,
        message: MessageChain,
        subject: Contact,
        args: List<String>
    ): ResultMessage? {
        var r18 = 0
        var num = 1
        val sb = StringBuilder()
        args.forEach {
            it.lowercase()
            when {
                it == "r" -> r18 = 1
                it.startsWith("n") || it.startsWith("num") ->
                    num = Pattern.compile("[^0-9]").matcher(it).replaceAll("").toInt()

                else -> sb.append("tag=").append(URLEncoder.encode(it, StandardCharsets.UTF_8)).append("&")
            }
        }

        val response = HttpUtil.getString(url + "?" + sb + "r18=" + r18 + "&num=" + num)
        if (response?.statusCode() != 200) return fail()
        val data = JSONUtil.parseObj(response.body()).getJSONArray("data")
        val forwardBuilder = ForwardMessageBuilder(subject)

        GlobalScope.launch {
            data.forEach {
                launch {
                    val url = JSONUtil.parseObj(it).getJSONObject("urls").getStr("original")
                    val result = HttpUtil.getInputStreamByProxy(url)
                    if (result?.statusCode() == 200) {
                        val image = subject.uploadImage(result.body())
                        forwardBuilder.add(subject.bot, image)
                    }
                }
            }
        }.join()

        if (forwardBuilder.size > 0) {
            return result(forwardBuilder.build().toMessageChain())
        }
        return result("没有找到符合条件的涩图")
    }
}