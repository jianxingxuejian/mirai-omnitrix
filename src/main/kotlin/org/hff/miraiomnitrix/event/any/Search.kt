package org.hff.miraiomnitrix.event.any

import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.buildMessageChain
import org.hff.miraiomnitrix.config.AccountProperties
import org.hff.miraiomnitrix.event.*
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.JsonUtil
import org.hff.miraiomnitrix.utils.getInfo

@Event(priority = 4)
class Search(private val accountProperties: AccountProperties) : AnyEvent {

    private val url = "https://saucenao.com/search.php?db=999&output_type=2&numres=1&api_key="
    private val commands = listOf("st", "搜图", "soutu")
    override suspend fun handle(args: List<String>, event: MessageEvent): EventResult {
        if (args.isEmpty()) return next()
        if (!commands.any { it in args }) return next()
        val saucenaoKey = accountProperties.saucenaoKey ?: return stop("未配置SauceNAO的Key")

        val (subject, _, message) = event.getInfo()

        val quote = message[QuoteReply.Key]
        val image = if (quote != null) {
            val imageId = Cache.imageCache.getIfPresent(quote.source.internalIds[0]) ?: return next()
            Image(imageId)
        } else message[Image.Key] ?: return next()

        val json = HttpUtil.getStringByProxy("$url$saucenaoKey&url=${image.queryUrl()}")
        val results: List<Result> = JsonUtil.fromJson(json, "results")
        if (results.isEmpty()) return stop()
        val (header, data) = results[0]
        val urls = data.ext_urls
        if (urls.isNullOrEmpty()) return stop("无搜图结果")
        val similarity = header.similarity.toDouble()
        if (similarity < 60) return stop("相似度过低")
        val thumbnailImg = HttpUtil.getInputStreamByProxy(header.thumbnail)
        val urlsText = if (urls.size == 1) {
            "链接: " + urls[0].trim('"')
        } else {
            urls.mapIndexed { index, url -> "链接${index + 1}：${url.trim('"')}" }.joinToString("\n")
        }
        buildMessageChain {
            +"搜图结果：\n"
            +subject.uploadImage(thumbnailImg)
            +"相似度：$similarity\n"
            if (data.title != null) +"标题：${data.title}\n"
            +"$urlsText\n"
            val author = data.member_name ?: data.user_name ?: data.creator ?: data.jp_name
            if (author != null) +"作者：$author"
        }.apply { return stop(this) }
    }

    data class Result(
        val header: Header,
        val data: Data
    )

    data class Header(
        val similarity: String,
        val thumbnail: String
    )

    data class Data(
        val ext_urls: List<String>?,
        val title: String?,
        val member_name: String?,
        val user_name: String?,
        val creator: String?,
        val jp_name: String?
    )
}