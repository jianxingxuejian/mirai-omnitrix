package org.hff.miraiomnitrix.event.any

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.QuoteReply
import org.hff.miraiomnitrix.config.AccountProperties
import org.hff.miraiomnitrix.result.EventResult
import org.hff.miraiomnitrix.result.EventResult.Companion.next
import org.hff.miraiomnitrix.result.EventResult.Companion.stop
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.ImageUtil
import org.hff.miraiomnitrix.utils.JsonUtil
import org.hff.miraiomnitrix.utils.JsonUtil.getAsStr
import org.hff.miraiomnitrix.utils.JsonUtil.getAsStrOrNull
import org.hff.miraiomnitrix.utils.SpringUtil

object Search : AnyEvent {

    private const val url = "https://saucenao.com/search.php?db=999&output_type=2&numres=1&api_key="
    private val key = SpringUtil.getBean(AccountProperties::class)?.saucenaoKey
    private val commands = listOf("st", "搜图", "soutu")
    override suspend fun handle(
        sender: User,
        message: MessageChain,
        subject: Contact,
        args: List<String>,
        event: MessageEvent
    ): EventResult {
        if (args.isEmpty()) return next()

        if (!commands.contains(args[0])) return next()
        val quote = message[QuoteReply.Key]
        val image = if (quote != null) {
            val imageId =
                ImageUtil.imageCache.getIfPresent(quote.source.internalIds[0]) ?: return next()
            Image(imageId)
        } else {
            message[Image.Key] ?: return next()
        }

        val apiResult = HttpUtil.getStringByProxy("$url$key&url=${image.queryUrl()}")
        val array = JsonUtil.getArray(apiResult, "results")
        if (array.isEmpty) return stop()
        val result = array[0].asJsonObject
        val header = result.getAsJsonObject("header")
        val data = result.getAsJsonObject("data")
        val thumbnail = header.getAsStr("thumbnail")
        val thumbnailImg = HttpUtil.getInputStreamByProxy(thumbnail)
        val urls = data.getAsJsonArray("ext_urls")
        val urlsText = if (urls.size() == 1) {
            "链接: " + urls[0]
        } else {
            urls.mapIndexed { index, url -> "链接${index + 1}：$url" }.joinToString("\n")
        }
        val chain = MessageChainBuilder()
            .append("搜图结果：\n")
            .append(subject.uploadImage(thumbnailImg))
            .append("相似度：" + header.getAsStr("similarity") + "\n")
            .append("标题：").append(data.getAsStrOrNull("title")).append("\n")
            .append(urlsText + "\n")
            .append("作者：").append(
                data.getAsStrOrNull("member_name") ?: data.getAsStrOrNull("user_name")
                ?: data.getAsStrOrNull("creator") ?: data.getAsStrOrNull("jp_name")
            )
            .build()
        return stop(chain)
    }
}