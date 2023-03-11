package org.hff.miraiomnitrix.event.any

import net.mamoe.mirai.contact.Contact
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
import org.jsoup.Jsoup

@Event(priority = 4)
class Search(private val accountProperties: AccountProperties) : AnyEvent {

    private val url = "https://saucenao.com/search.php?db=999&output_type=2&numres=1&api_key="
    private val commands = listOf("st", "搜图", "soutu")
    override suspend fun handle(args: List<String>, event: MessageEvent, isAt: Boolean): EventResult {
        if (args.isEmpty()) return next()
        if (!commands.any { it in args }) return next()

        val (subject, _, message) = event.getInfo()

        val quote = message[QuoteReply.Key]
        val imgUrl = if (quote != null) {
            val imageId = Cache.imageCache.getIfPresent(quote.source.internalIds[0]) ?: return next()
            Image(imageId).queryUrl()
        } else message[Image.Key]?.queryUrl() ?: return next()

        trySauceNAO(imgUrl, subject)

        return stop()
    }

    suspend fun trySauceNAO(imgUrl: String, subject: Contact): Boolean {
        val saucenaoKey = accountProperties.saucenaoKey ?: return false
        val json = HttpUtil.getStringByProxy("$url$saucenaoKey&url=$imgUrl")
        val results: List<SauceNAOResult> = JsonUtil.fromJson(json, "results")
        if (results.isEmpty()) {
            subject.sendMessage("SauceNAO未找到搜图结果")
            return false
        }
        val (header, data) = results[0]
        val urls = data.ext_urls
        if (urls.isNullOrEmpty()) {
            subject.sendMessage("SauceNAO未找到搜图结果")
            return false
        }
        val similarity = header.similarity.toDouble()
        if (similarity < 60) {
            subject.sendMessage("SauceNAO搜图结果相似度过低，切换下一个引擎")
            return false
        }
        val thumbnailImg = HttpUtil.getInputStreamByProxy(header.thumbnail)
        val urlsText = if (urls.size == 1) {
            "链接: " + urls[0].trim('"')
        } else {
            urls.mapIndexed { index, url -> "链接${index + 1}：${url.trim('"')}" }.joinToString("\n")
        }
        buildMessageChain {
            +"SauceNAO搜图结果：\n"
            +subject.uploadImage(thumbnailImg)
            +"相似度：$similarity\n"
            if (data.title != null) +"标题：${data.title}\n"
            +"$urlsText\n"
            val author = data.member_name ?: data.user_name ?: data.creator ?: data.jp_name
            if (author != null) +"作者：$author"
        }.apply { return true }
    }

    // 使用图片url请求ascii2d网站的搜图数据，解析第一条的html数据
    suspend fun ascii2d(imgUrl: String, subject: Contact): Boolean {
        val html = HttpUtil.getStringByProxy("https://ascii2d.net/search/url/$imgUrl")
        val doc = Jsoup.parse(html)
        val elements = doc.select("div[class=card-body]")
        if (elements.size < 2) {
            subject.sendMessage("ascii2d未找到搜图结果")
            return false
        }
        val element = elements[1]
        val similarity = element.select("div[class=card-text]").text().split(" ")[1].toDouble()
        if (similarity < 60) {
            subject.sendMessage("ascii2d搜图结果相似度过低，切换下一个引擎")
            return false
        }
        val thumbnailImg = HttpUtil.getInputStreamByProxy(element.select("img").attr("src"))
        val urls = element.select("a").map { it.attr("href") }
        val urlsText = if (urls.size == 1) {
            "链接: " + urls[0].trim('"')
        } else {
            urls.mapIndexed { index, url -> "链接${index + 1}：${url.trim('"')}" }.joinToString("\n")
        }
        buildMessageChain {
            +"ascii2d搜图结果：\n"
            +subject.uploadImage(thumbnailImg)
            +"相似度：$similarity\n"
            +"$urlsText\n"
        }.apply { return true }
    }

    data class SauceNAOResult(
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