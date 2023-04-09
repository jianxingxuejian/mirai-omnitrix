package org.hff.miraiomnitrix.event.group

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.buildMessageChain
import org.apache.ibatis.ognl.DynamicSubscript.first
import org.hff.miraiomnitrix.config.AccountProperties
import org.hff.miraiomnitrix.event.*
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.JsonUtil
import org.hff.miraiomnitrix.utils.uploadImage

@Event(priority = 4)
class LinkParse(accountProperties: AccountProperties) : GroupEvent {

    private val bvRegex = """(?i)(?<!\w)(?:av(\d+)|(BV1[1-9A-NP-Za-km-z]{9}))""".toRegex()
    private val v2exRegex = """(?:https?://)?(?:www\.)?v2ex\.com/t/(\d+)""".toRegex()
    private val v2exToken = accountProperties.v2exToken

    override suspend fun GroupMessageEvent.handle(args: List<String>, isAt: Boolean): EventResult {
        val link = args.getOrNull(0) ?: return stop()
        if (link.length > 80) return next()

        val bvMatch = bvRegex.find(link)
        if (bvMatch != null) {
            val value = bvMatch.groups[0]?.value ?: return stop()
            return parseBilibili(value).let(::stop)
        }

        val v2exMatch = v2exRegex.find(link)
        if (v2exMatch != null) {
            val value = v2exMatch.groups[1]?.value ?: return stop()
            return parseV2ex(value)?.let(::stop) ?: stop()
        }

        return next()
    }

    private suspend fun GroupMessageEvent.parseBilibili(value: String): MessageChain {
        val param = when {
            value.startsWith("a") -> mapOf("aid" to value)
            else -> mapOf("bvid" to value)
        }
        val json = HttpUtil.getString("https://api.bilibili.com/x/web-interface/view", param)
        val data: BiliVideoInfo = JsonUtil.fromJson(json, "data")
        return buildMessageChain {
            +uploadImage(HttpUtil.getInputStream(data.pic))
            +"标题：${data.title}\n"
            +"简介：${data.desc.take(100)}${if (data.desc.length > 100) "……" else ""}\n"
            +"UP主: ${data.owner.name}\n"
            +"链接：https://www.bilibili.com/video/$first\n"
        }
    }

    private suspend fun parseV2ex(value: String): MessageChain? {
        if (v2exToken == null) return null
        val headers = mapOf("Authorization" to "Bearer $v2exToken")
        val data: Topic = HttpUtil.getJson("https://www.v2ex.com/api/v2/topics/$value", headers, true)
        if (!data.success) return null
        with(data.result) {
            return buildMessageChain {
                +"标题：$title\n"
                +"内容：${content.take(100)}${if (content.length > 100) "……" else ""}\n"
                +"所属节点: ${node.title}\n"
                +"链接：${url}\n"
            }
        }
    }

    data class BiliVideoInfo(val pic: String, val title: String, val desc: String, val owner: Owner)
    data class Owner(val name: String)

    data class Topic(val message: String, val result: Result, val success: Boolean)
    data class Result(
        val content: String,
        val node: Node,
        val supplements: List<Any>,
        val title: String,
        val url: String
    )

    data class Node(val title: String)

}
