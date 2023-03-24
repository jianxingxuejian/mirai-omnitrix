package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.config.AccountProperties
import org.hff.miraiomnitrix.exception.MyException
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.JsonUtil

@Command(name = ["img", "image", "画图"])
class Image(accountProperties: AccountProperties) : AnyCommand {

    private val apiKey = accountProperties.openaiApiKey?.let { "Bearer $it" }

    override suspend fun execute(args: List<String>, event: MessageEvent): Message? {
        if (args.isEmpty()) return null
        val subject = event.subject
        return generations(args.joinToString(" "), subject)
    }

    suspend fun generations(prompt: String, subject: Contact): Image {
        if (apiKey == null) throw MyException("未配置apikey")
        val data = mapOf("prompt" to prompt)
        val headers = mapOf("Authorization" to apiKey)
        val json = HttpUtil.postStringByProxy("https://api.openai.com/v1/images/generations", data, headers)
        val url = JsonUtil.getArray(json, "data")[0].asString
        HttpUtil.getInputStream(url).use { return subject.uploadImage(it) }
    }

}
