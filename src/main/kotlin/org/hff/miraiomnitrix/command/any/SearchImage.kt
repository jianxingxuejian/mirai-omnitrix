package org.hff.miraiomnitrix.command.any

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
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
import org.hff.miraiomnitrix.utils.*
import org.jsoup.Jsoup

@Command(name = ["st", "搜图", "soutu", "hero"])
class SearchImage(accountProperties: AccountProperties) : AnyCommand {

    override val needHead = false

    private val sauceNAOUrl = "https://saucenao.com/search.php?db=999&output_type=2&numres=1&api_key="
    private val saucenaoKey = accountProperties.saucenaoKey
    private val ascii2d = "https://ascii2d.net"

    override suspend fun MessageEvent.execute(args: List<String>): Message? {
        val imgUrl = message.getImage()?.queryUrl() ?: return "未找到图片，重新发送图片+关键字重试".toPlainText()
        if (!sauceNAO(imgUrl)) ascii2d(imgUrl)
        return null
    }

    private suspend inline fun MessageEvent.sauceNAO(imgUrl: String): Boolean {
        if (saucenaoKey == null) throw MyException("未配置SauceNAO Key")
        val json = HttpUtil.getString("$sauceNAOUrl$saucenaoKey&url=$imgUrl", isProxy = true)
        val results = JsonUtil.getArray(json, "results")
        if (results.isEmpty) {
            subject.sendMessage("SauceNAO未找到搜图结果，切换下一个引擎")
            return false
        }
        val result = results[0].asJsonObject
        val header = result.getAsObj("header")
        val similarity = header.getAsStr("similarity").toDouble()
        if (similarity < 60) {
            subject.sendMessage("SauceNAO搜图结果相似度过低，切换下一个引擎")
            return false
        }
        val data = result.getAsObj("data")
        val pixiv_id = data.getAsStrOrNull("pixiv_id")
        val ext_urls = data.getAsArrayOrNull("ext_urls")
        val urls =
            if (pixiv_id != null) "链接: https://www.pixiv.net/artworks/$pixiv_id"
            else ext_urls?.run {
                if (size() == 1) {
                    "链接: " + first().asString.trim('"')
                } else {
                    mapIndexed { index, url -> "链接${index + 1}：${url.asString.trim('"')}" }.joinToString("\n")
                }
            }
        val thumbnail = header.getAsStr("thumbnail")
        val title = data.getAsStrOrNull("title")
        val source = data.getAsStrOrNull("source")
        val jp_name = data.getAsStrOrNull("jp_name")
        val eng_name = data.getAsStrOrNull("eng_name")
        val name = jp_name ?: eng_name
        val creator = data.get("creator")
        val author_name = data.getAsStrOrNull("author_name")
        val member_name = data.get("member_name")
        val user_name = data.getAsStrOrNull("user_name")
        val author = author_name ?: creator?.arrayOrStrToStr() ?: member_name?.arrayOrStrToStr() ?: user_name
        buildMessageChain {
            +"SauceNAO搜图结果：\n"
            +uploadImage(HttpUtil.getInputStream(thumbnail, isProxy = true))
            +"相似度：$similarity\n"
            if (title != null) +"标题：$title\n"
            if (source != null) +"来源：$source\n"
            if (urls != null) +"$urls\n"
            if (name != null) +"名称：$name\n"
            if (author != null) +"作者：$author\n"
        }.let { send(it) }
        return true
    }

    private fun JsonElement.arrayOrStrToStr(): String? = when (this) {
        is JsonArray -> joinToString("，") { it.asString }
        is JsonPrimitive -> asString
        else -> null
    }

    private suspend fun MessageEvent.ascii2d(imgUrl: String): Boolean {
        val headers = mapOf("User-Agent" to "PostmanRuntime/7.31.3")
        val bovwLink = HttpUtil.getString("$ascii2d/search/url/$imgUrl", headers, true).let {
            Jsoup.parse(it).getElementsByClass("detail-link").first()?.child(1)?.child(0)?.attr("href")
        }
        if (bovwLink == null) {
            send("ascii2d未找到搜图结果")
            return false
        }
        val search = HttpUtil.getString(ascii2d + bovwLink, isProxy = true).let {
            Jsoup.parse(it).getElementsByClass("item-box")[1]
        }
        val detail = search.child(1).child(3).child(0)
        if (detail.childrenSize() == 1) {
            send("ascii2d未找到搜图结果")
            return false
        }
        val thumbnailUrl = search.child(0).child(0).attr("src")
        val thumbnail = HttpUtil.getInputStream(ascii2d + thumbnailUrl, isProxy = true)
        val origin = detail.child(0).attr("alt")
        val url = detail.child(1).attr("href")
        val title = detail.child(1).text()
        val author = detail.child(2).text()
        buildMessageChain {
            +"ascii2d搜图结果：\n"
            +uploadImage(thumbnail)
            +"来源：$origin\n"
            +"标题：$title\n"
            +"作者：$author\n"
            +"链接：$url\n"
        }.let { send(it) }
        return true
    }

}
