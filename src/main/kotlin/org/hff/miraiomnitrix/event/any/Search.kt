package org.hff.miraiomnitrix.event.any

import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.QuoteReply
import org.hff.miraiomnitrix.config.AccountProperties
import org.hff.miraiomnitrix.event.*
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.JsonUtil
import org.hff.miraiomnitrix.utils.JsonUtil.getArrayOrNull
import org.hff.miraiomnitrix.utils.JsonUtil.getAsStr
import org.hff.miraiomnitrix.utils.JsonUtil.getAsStrOrNull
import org.hff.miraiomnitrix.utils.getInfo

@Event(priority = 4)
class Search(private val accountProperties: AccountProperties) : AnyEvent {

    private val url = "https://saucenao.com/search.php?db=999&output_type=2&numres=1&api_key="
    private val commands = listOf("st", "搜图", "soutu")
    override suspend fun handle(args: List<String>, event: MessageEvent): EventResult {
        if (args.isEmpty()) return next()
        if (!commands.contains(args[0])) return next()

        val (subject, _, message) = event.getInfo()

        val quote = message[QuoteReply.Key]
        val image = if (quote != null) {
            val imageId = Cache.imageCache.getIfPresent(quote.source.internalIds[0]) ?: return next()
            Image(imageId)
        } else message[Image.Key] ?: return next()

        val apiResult = HttpUtil.getStringByProxy("$url${accountProperties.saucenaoKey}&url=${image.queryUrl()}")
        val array = JsonUtil.getArray(apiResult, "results")
        if (array.isEmpty) return stop()
        val result = array[0].asJsonObject
        val header = result.getAsJsonObject("header")
        val data = result.getAsJsonObject("data")
        val thumbnail = header.getAsStr("thumbnail")
        val thumbnailImg = HttpUtil.getInputStreamByProxy(thumbnail)
        val urls = data.getArrayOrNull("ext_urls")
        if (urls == null || urls.isEmpty) return stop("无搜图结果")
        val similarity = header.getAsStr("similarity")
        if (similarity.toDouble() < 60) return stop("相似度过低")
        val urlsText = if (urls.size() == 1) {
            "链接: " + urls[0].asString.trim('"')
        } else {
            urls.mapIndexed { index, url -> "链接${index + 1}：${url.asString.trim('"')}" }.joinToString("\n")
        }
        val builder = MessageChainBuilder()
            .append("搜图结果：\n")
            .append(subject.uploadImage(thumbnailImg))
            .append("相似度：$similarity\n")
        val title = data.getAsStrOrNull("title")
        if (title != null) builder.append("标题：").append(title).append("\n")
        builder.append(urlsText + "\n")
        val author = data.getAsStrOrNull("member_name") ?: data.getAsStrOrNull("user_name")
        ?: data.getAsStrOrNull("creator") ?: data.getAsStrOrNull("jp_name")
        if (author != null) builder.append("作者：").append(author)
        return stop(builder.build())
    }
}