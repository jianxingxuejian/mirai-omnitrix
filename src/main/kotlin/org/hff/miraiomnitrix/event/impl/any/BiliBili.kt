package org.hff.miraiomnitrix.event.impl.any

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.RichMessage
import org.hff.miraiomnitrix.event.type.AnyHandler
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.JsonUtil
import org.hff.miraiomnitrix.utils.JsonUtil.getAsStr

object BiliBili : AnyHandler {

    private const val VIDEO_API = "https://api.bilibili.com/x/web-interface/view"
    private val VIDEO_REGEX = """(?i)(?<!\w)(?:av(\d+)|(BV1[1-9A-NP-Za-km-z]{9}))""".toRegex()
    override suspend fun handle(sender: User, message: MessageChain, subject: Contact, args: List<String>): Boolean {
        if (args.isEmpty()) return false

        val first = args[0]
        if (first.length < 30 && VIDEO_REGEX matches args[0]) {
            val json = HttpUtil.getString(VIDEO_API, mapOf("bvid" to first))
            val data = JsonUtil.getObj(json, "data")
            val content = data.getAsStr("title")
            val share = RichMessage.Key.share(
                "https://www.bilibili.com/video/$first",
                "哔哩哔哩",
                content,
                "https://open.gtimg.cn/open/app_icon/00/95/17/76/100951776_100_m.png?t=1675158231"
            )
//            val share = LightApp(
//                ""
//            )
//            val detail = Detail(desc = content, preview = pic)
//            val meta = Meta(detail)
//            val info = BiliVideoInfo(meta = meta)
//            val share = LightApp(JsonUtil.toJson(info))
            subject.sendMessage(share)
        }
        return false
    }

    data class BiliVideoInfo(
        val app: String = "com.tencent.miniapp_01",
        val desc: String = "哔哩哔哩",
        val ver: String = "1.0.0.19",
        val prompt: String = "[QQ小程序]哔哩哔哩",
        val needShareCallBack: Boolean = false,
        val meta: Meta
    )

    data class Meta(
        val detail_1: Detail
    )

    data class Detail(
        val desc: String,
        val icon: String = "https://open.gtimg.cn/open/app_icon/00/95/17/76/100951776_100_m.png?t=1675158231",
        val preview: String,
        val title: String = "哔哩哔哩"
    )

}