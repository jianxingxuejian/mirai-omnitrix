package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.Message
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.config.AccountProperties
import org.hff.miraiomnitrix.exception.MyException
import org.hff.miraiomnitrix.utils.*
import org.hff.miraiomnitrix.utils.ImageUtil.toImmutableImage
import org.hff.miraiomnitrix.utils.ImageUtil.toStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@Command(name = ["img", "image", "画图"])
class Image(accountProperties: AccountProperties) : AnyCommand {

    private val apiKey = accountProperties.openaiApiKey?.let { "Bearer $it" }

    override suspend fun execute(args: List<String>, event: MessageEvent): Message? {
        val (subject, _, message) = event.getInfo()
        return when (val image = message.getImage()) {
            null -> {
                when {
                    args.isEmpty() -> null
                    else -> generations(args.joinToString(" "), subject)
                }
            }

            else -> variations(image.queryUrl(), subject)
        }
    }

    suspend fun generations(prompt: String, subject: Contact): Image {
        if (apiKey == null) throw MyException("未配置apikey")
        val data = mapOf("prompt" to prompt)
        val headers = mapOf("Authorization" to apiKey)
        val json = HttpUtil.postStringByProxy("https://api.openai.com/v1/images/generations", data, headers)
        val url = JsonUtil.getArray(json, "data")[0].getAsStr("url")
        HttpUtil.getInputStream(url).use { return subject.uploadImage(it) }
    }

    suspend fun variations(imgUrl: String, subject: Contact): Image {
        if (apiKey == null) throw MyException("未配置apikey")
        val headers = mapOf("Authorization" to apiKey)
        val image = HttpUtil.getInputStream(imgUrl).toImmutableImage()
        val width = image.width.coerceAtMost(image.height)
        val file = image.scaleTo(width, width).toStream().use {
            RequestFile(
                name = "image",
                filename = "image.png",
                mimeType = "image/png",
                fileBytes = it.readBytes()
            )
        }
        val json = HttpUtil.postFormByProxy("https://api.openai.com/v1/images/variations", file, headers)
        val url = JsonUtil.getArray(json, "data")[0].getAsStr("url")
        HttpUtil.getInputStream(url).use { return subject.uploadImage(it) }
    }

}
