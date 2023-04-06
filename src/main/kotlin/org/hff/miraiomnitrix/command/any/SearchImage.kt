package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.common.MyException
import org.hff.miraiomnitrix.common.getImage
import org.hff.miraiomnitrix.config.AccountProperties
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.JsonUtil
import org.jsoup.Jsoup

@Command(name = ["st", "搜图", "soutu"], needHead = false)
class SearchImage(accountProperties: AccountProperties) : AnyCommand {

    private val sauceNAOUrl = "https://saucenao.com/search.php?db=999&output_type=2&numres=1&api_key="
    private val saucenaoKey = accountProperties.saucenaoKey

    override suspend fun MessageEvent.execute(args: List<String>): Message? {
        val imgUrl = message.getImage()?.queryUrl() ?: return "未找到图片，重新发送图片+关键字重试".toPlainText()
        if (!sauceNAO(imgUrl, subject)) ascii2d(imgUrl, subject)
        return null
    }

    private suspend fun sauceNAO(imgUrl: String, subject: Contact): Boolean {
        if (saucenaoKey == null) throw MyException("未配置SauceNAO Key")
        val json = HttpUtil.getStringByProxy("$sauceNAOUrl$saucenaoKey&url=$imgUrl")
        val results: List<SauceNAOResult>? = JsonUtil.fromJson(json, "results")
        if (results.isNullOrEmpty()) {
            subject.sendMessage("SauceNAO未找到搜图结果，切换下一个引擎")
            return false
        }
        val (header, data) = results[0]
        val urls = data.ext_urls
        if (urls.isNullOrEmpty()) {
            subject.sendMessage("SauceNAO未找到搜图结果，切换下一个引擎")
            return false
        }
        val similarity = header.similarity.toDouble()
        if (similarity < 60) {
            subject.sendMessage("SauceNAO搜图结果相似度过低，切换下一个引擎")
            return false
        }
        val urlsText = if (urls.size == 1) {
            "链接: " + urls[0].trim('"')
        } else {
            urls.mapIndexed { index, url -> "链接${index + 1}：${url.trim('"')}" }.joinToString("\n")
        }
        buildMessageChain {
            +"SauceNAO搜图结果：\n"
            +HttpUtil.getInputStreamByProxy(header.thumbnail).use { subject.uploadImage(it) }
            +"相似度：$similarity\n"
            if (data.title != null) +"标题：${data.title}\n"
            +"$urlsText\n"
            val author = data.member_name ?: data.user_name ?: data.creator ?: data.jp_name
            if (author != null) +"作者：$author"
        }.run { subject.sendMessage(this) }
        return true
    }

    private suspend fun ascii2d(imgUrl: String, subject: Contact): Boolean {
        val headers = mapOf("User-Agent" to "PostmanRuntime/7.31.3")
        val bovwLink = HttpUtil.getStringByProxy("https://ascii2d.net/search/url/$imgUrl", headers).let {
            Jsoup.parse(it).getElementsByClass("detail-link").first()?.child(1)?.child(0)?.attr("href")
        }
        if (bovwLink == null) {
            subject.sendMessage("ascii2d未找到搜图结果")
            return false
        }
        val search = HttpUtil.getStringByProxy("https://ascii2d.net$bovwLink").let {
            Jsoup.parse(it).getElementsByClass("item-box")[1]
        }
        val detail = search.child(1).child(3).child(0)
        if (detail.childrenSize() == 1) {
            subject.sendMessage("ascii2d未找到搜图结果")
            return false
        }
        val thumbnailUrl = search.child(0).child(0).attr("src")
        val thumbnail = HttpUtil.getInputStreamByProxy(thumbnailUrl.let { "https://ascii2d.net$it" })
        val origin = detail.child(0).attr("alt")
        val url = detail.child(1).attr("href")
        val title = detail.child(1).text()
        val author = detail.child(2).text()
        buildMessageChain {
            +"ascii2d搜图结果：\n"
            +thumbnail.use { subject.uploadImage(it) }
            +"来源：$origin\n"
            +"标题：$title\n"
            +"作者：$author\n"
            +"链接：$url\n"
        }.run { subject.sendMessage(this) }
        return true
    }

    private data class SauceNAOResult(
        val header: Header,
        val data: Data
    )

    private data class Header(
        val similarity: String,
        val thumbnail: String
    )

    private data class Data(
        val ext_urls: List<String>?,
        val title: String?,
        val member_name: String?,
        val user_name: String?,
        val creator: String?,
        val jp_name: String?
    )
}
