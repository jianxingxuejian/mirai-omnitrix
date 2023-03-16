package org.hff.miraiomnitrix.event.any

import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.buildMessageChain
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.event.*
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.JsonUtil

@Event(priority = 2)
class BiliBili(private val permissionProperties: PermissionProperties) : AnyEvent {

    private val videoUrl = "https://api.bilibili.com/x/web-interface/view"
    private val avRegex = """(?i)(?<!\w)av(\d+)""".toRegex()
    private val bvRegex = """(?i)(?<!\w)BV1[1-9A-NP-Za-km-z]{9}""".toRegex()
    override suspend fun handle(args: List<String>, event: MessageEvent, isAt: Boolean): EventResult {
        if (args.isEmpty()) return next()

        val subject = event.subject
        if (permissionProperties.bvExcludeGroup.contains(subject.id)) return next()

        val first = args[0]
        if (first.length > 30) return next()

        val param = when {
            avRegex.matches(first) -> mapOf("aid" to avRegex.find(first)!!.groups[1]!!.value)
            bvRegex.matches(first) -> mapOf("bvid" to first)
            else -> return next()
        }
        val json = HttpUtil.getString(videoUrl, param)
        val data: BiliVideoInfo = JsonUtil.fromJson(json, "data")
        val img = HttpUtil.getInputStream(data.pic).use { subject.uploadImage(it) }
        buildMessageChain {
            +img
            +"标题：${data.title}\n"
            +"简介：${data.desc.take(100)}${if (data.desc.length > 100) "……" else ""}\n"
            +"UP主: ${data.owner.name}\n"
            +"链接：https://www.bilibili.com/video/$first\n"
        }.apply { return stop(this) }
    }

    data class BiliVideoInfo(
        val pic: String,
        val title: String,
        val desc: String,
        val owner: Owner
    )

    data class Owner(val name: String)

}
