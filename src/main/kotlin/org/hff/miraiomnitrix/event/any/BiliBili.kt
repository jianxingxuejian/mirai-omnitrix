package org.hff.miraiomnitrix.event.any

import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChainBuilder
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.event.*
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.JsonUtil
import org.hff.miraiomnitrix.utils.JsonUtil.getAsStr

@Event(priority = 2)
class BiliBili(private val permissionProperties: PermissionProperties) : AnyEvent {

    private val videoUrl = "https://api.bilibili.com/x/web-interface/view"
    private val videoRegex = """(?i)(?<!\w)(?:av(\d+)|(BV1[1-9A-NP-Za-km-z]{9}))""".toRegex()
    override suspend fun handle(args: List<String>, event: MessageEvent): EventResult {
        if (args.isEmpty()) return next()

        val subject = event.subject
        if (permissionProperties.bvExcludeGroup.contains(subject.id)) return next()

        val first = args[0]
        if (first.length > 30 || !(videoRegex matches args[0])) return next()

        val json = HttpUtil.getString(videoUrl, mapOf("bvid" to first))
        val data = JsonUtil.getObj(json, "data")
        val title = data.getAsStr("title")
        val picUrl = data.getAsStr("pic")
        val pic = subject.uploadImage(HttpUtil.getInputStream(picUrl))
        val desc = data.getAsStr("desc")
        val share = MessageChainBuilder()
            .append("哔哩哔哩链接解析：\n")
            .append("标题：$title\n")
            .append(pic)
            .append("简介：$desc\n")
            .append("链接：https://www.bilibili.com/video/$first")
            .build()
        return stop(share)
//            val share = RichMessage.Key.share(
//                "https://www.bilibili.com/video/$first",
//                "哔哩哔哩",
//                content,
//                "https://open.gtimg.cn/open/app_icon/00/95/17/76/100951776_100_m.png?t=1675158231"
//            )

//            val detail = Detail(desc = content, preview = pic)
//            val meta = Meta(detail)
//            val info = BiliVideoInfo(meta = meta)
//            val share = LightApp(JsonUtil.toJson(info))
    }

//    data class BiliVideoInfo(
//        val app: String = "com.tencent.miniapp_01",
//        val desc: String = "哔哩哔哩",
//        val ver: String = "1.0.0.19",
//        val prompt: String = "[QQ小程序]哔哩哔哩",
//        val needShareCallBack: Boolean = false,
//        val meta: Meta
//    )
//
//    data class Meta(
//        val detail_1: Detail
//    )
//
//    data class Detail(
//        val desc: String,
//        val icon: String = "https://open.gtimg.cn/open/app_icon/00/95/17/76/100951776_100_m.png?t=1675158231",
//        val preview: String,
//        val title: String = "哔哩哔哩"
//    )

}