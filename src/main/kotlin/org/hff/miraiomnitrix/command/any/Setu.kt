package org.hff.miraiomnitrix.command.any

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.ForwardMessageBuilder
import net.mamoe.mirai.message.data.toMessageChain
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.CommandResult
import org.hff.miraiomnitrix.command.result
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.JsonUtil
import org.hff.miraiomnitrix.utils.JsonUtil.get
import org.hff.miraiomnitrix.utils.JsonUtil.getAsStr
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


@Command(name = ["涩图", "setu"])
class Setu : AnyCommand {

    private val api1 = "https://api.lolicon.app/setu/v2"
    private val api2 = "https://image.anosu.top/pixiv/json"

    override suspend fun execute(args: List<String>, event: MessageEvent): CommandResult? {
        var r18 = 0
        var num = 1
        val keywords = mutableListOf<String>()
        args.forEach { arg ->
            when {
                arg == "r" || arg == "r18" || arg == "R" || arg == "R18" -> r18 = 1
                arg.matches(Regex("^(n|num)[0-9]+$")) -> num = arg.substringAfter("n", "num").toInt()
                else -> keywords.add(arg.lowercase())
            }
        }
        val subject = event.subject
        val forwardBuilder = ForwardMessageBuilder(subject)

        coroutineScope {
            val exceptionHandler = CoroutineExceptionHandler { _, _ ->
                run {
                    val tags = keywords.joinToString("&tag=", "&tag=") { URLEncoder.encode(it, StandardCharsets.UTF_8) }
                    val json = HttpUtil.getString("$api1?r18=$r18&num=$num$tags")
                    JsonUtil.getArray(json, "data")
                        .map { it.get("urls").getAsStr("original") }
                        .forEach { launch { addForward(forwardBuilder, subject, it) } }
                }
            }
            launch(exceptionHandler) {
                val json = HttpUtil.getString("$api2?r18=$r18&num=$num&keyword=${keywords.joinToString("|")}")
                JsonUtil.getArray(json).map { it.getAsStr("url") }
                    .forEach {
                        try {
                            launch { addForward(forwardBuilder, subject, it) }
                        } catch (_: Exception) {
                            return@forEach
                        }
                    }
            }
        }

        if (forwardBuilder.size > 0) {
            subject.sendMessage(forwardBuilder.build().toMessageChain()).apply { if (r18 == 1) recallIn(60 * 1000) }
            return null
        }
        return result("没有找到符合条件的涩图")
    }

    suspend fun addForward(forwardBuilder: ForwardMessageBuilder, subject: Contact, imgUrl: String) {
        val result = HttpUtil.getInputStreamByProxy(imgUrl)
        val image = subject.uploadImage(result)
        forwardBuilder.add(subject.bot, image)
    }

}