package org.hff.miraiomnitrix.command.any

import io.ktor.client.call.*
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.utils.withUse
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.common.MyException
import org.hff.miraiomnitrix.common.getImage
import org.hff.miraiomnitrix.config.AccountProperties
import org.hff.miraiomnitrix.utils.*
import org.hff.miraiomnitrix.utils.ImageUtil.toImmutableImage
import org.hff.miraiomnitrix.utils.ImageUtil.toStream
import java.io.InputStream

@Command(name = ["img", "image"])
class Image(accountProperties: AccountProperties) : AnyCommand {

    override val needHead = false

    private val apiKey = accountProperties.openaiApiKey?.let { "Bearer $it" }
    private val generationsApi = "https://api.openai.com/v1/images/generations"
    private val variationsApi = "https://api.openai.com/v1/images/variations"

    override suspend fun MessageEvent.execute(args: List<String>): Message? {
        return when (val image = message.getImage()) {
            null -> when {
                args.isEmpty() -> "请发送图片或输入prompt".toPlainText()
                else -> uploadImageAndQuote(generations(args.joinToString(" ")))
            }

            else -> uploadImageAndQuote(variations(image.queryUrl()))
        }
    }

    suspend fun generations(prompt: String): InputStream {
        if (apiKey == null) throw MyException("未配置apikey")
        val data = mapOf("prompt" to prompt)
        val headers = mapOf("Authorization" to apiKey)
        return HttpUtil.post(generationsApi, data, headers, true).run {
            when (status.value) {
                200 -> JsonUtil.getArray(body(), "data")[0].getAsStr("url").let { HttpUtil.getInputStream(it) }
                else -> JsonUtil.getObj(body(), "error").getAsStr("message").let { throw MyException(it) }
            }
        }
    }

    suspend fun variations(imgUrl: String): InputStream {
        if (apiKey == null) throw MyException("未配置apikey")
        val headers = mapOf("Authorization" to apiKey)
        val image = HttpUtil.getInputStream(imgUrl).toImmutableImage()
        val width = image.width.coerceAtMost(image.height)
        val file = image.scaleTo(width, width).toStream().withUse { RequestFile(fileBytes = readBytes()) }
        val json = HttpUtil.formString(variationsApi, file, headers, true)
        return JsonUtil.getArray(json, "data")[0].getAsStr("url").let { HttpUtil.getInputStream(it) }
    }

}
