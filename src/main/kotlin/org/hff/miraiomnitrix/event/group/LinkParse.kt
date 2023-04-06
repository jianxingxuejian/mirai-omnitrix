package org.hff.miraiomnitrix.event.group

import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.buildMessageChain
import org.apache.ibatis.ognl.DynamicSubscript.first
import org.hff.miraiomnitrix.config.AccountProperties
import org.hff.miraiomnitrix.event.*
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.JsonUtil

@Event(priority = 2)
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
            return parseBilibili(value, group).let(::stop)
        }

        val v2exMatch = v2exRegex.find(link)
        if (v2exMatch != null) {
            val value = v2exMatch.groups[1]?.value ?: return stop()
            return parseV2ex(value)?.let(::stop) ?: stop()
        }

        return next()
    }

    private suspend fun parseBilibili(value: String, subject: Group): MessageChain {
        val param = when {
            value.startsWith("a") -> mapOf("aid" to value)
            else -> mapOf("bvid" to value)
        }
        val json = HttpUtil.getString("https://api.bilibili.com/x/web-interface/view", param)
        val data: BiliVideoInfo = JsonUtil.fromJson(json, "data")
        return buildMessageChain {
            +HttpUtil.getInputStream(data.pic).use { subject.uploadImage(it) }
            +"标题：${data.title}\n"
            +"简介：${data.desc.take(100)}${if (data.desc.length > 100) "……" else ""}\n"
            +"UP主: ${data.owner.name}\n"
            +"链接：https://www.bilibili.com/video/$first\n"
        }
    }

    private suspend fun parseV2ex(value: String): MessageChain? {
        if (v2exToken == null) return null
        val headers = mapOf("Authorization" to "Bearer $v2exToken")
        val json = HttpUtil.getStringByProxy("https://www.v2ex.com/api/v2/topics/$value", headers)
        val data: Topic = JsonUtil.fromJson(json)
        if (!data.success) return null
        val result = data.result
        return buildMessageChain {
            +"标题：${result.title}\n"
            +"内容：${result.content.take(100)}${if (result.content.length > 100) "……" else ""}\n"
            +"所属节点: ${result.node.title}\n"
            +"链接：${result.url}\n"
        }
    }

    data class BiliVideoInfo(
        val pic: String,
        val title: String,
        val desc: String,
        val owner: Owner
    )

    data class Owner(val name: String)

    data class Topic(
        val message: String,
        val result: Result,
        val success: Boolean
    )

    data class Result(
        val content: String,
        val content_rendered: String,
        val created: Int,
        val id: Int,
        val last_modified: Int,
        val last_reply_by: String,
        val last_touched: Int,
        val member: Member,
        val node: Node,
        val replies: Int,
        val supplements: List<Any>,
        val syntax: Int,
        val title: String,
        val url: String
    )

    data class Member(
        val avatar: String,
        val bio: String,
        val created: Int,
        val github: String,
        val id: Int,
        val url: String,
        val username: String,
        val website: String
    )

    data class Node(
        val avatar: String,
        val created: Int,
        val footer: String,
        val header: String,
        val id: Int,
        val last_modified: Int,
        val name: String,
        val title: String,
        val topics: Int,
        val url: String
    )

}
