package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.JsonUtil

@Command(name = ["壁纸", "bizhi"])
class Wallpaper : AnyCommand {

    private val urls = listOf(
        "http://api.iw233.cn/api.php?type=json&sort=",
        "http://ap1.iw233.cn/api.php?type=json&sort=",
        "https://dev.iw233.cn/api.php?type=json&sort=",
        "https://iw233.cn/api.php?type=json&sort="
    )

    private val vipUrl =
        "http://aikohfiosehgairl.fgimax2.fgnwctvip.com/uyfvnuvhgbuiesbrghiuudvbfkllsgdhngvbhsdfklbghdfsjksdhnvfgkhdfkslgvhhrjkdshgnverhbgkrthbklg.php?type=json&sort="
    private val vip = arrayOf(
        "qwuydcuqwgbvwgqefvbwgueahvbfkbegh",
        "dsrgvkbaergfvyagvbkjavfwe",
        "ergbskjhebrgkjlhkerjsbkbregsbg",
        "rsetbgsekbjlghelkrabvfgheiv"
    )

    override suspend fun MessageEvent.execute(args: List<String>): Message? {
        var sort = "random"
        args.forEach {
            when (it) {
                "随机", "suiji", "sj" -> sort = "random"
                "正常", "no", "normal" -> sort = "iw233"
                "推荐", "精选", "top" -> sort = "top"
                "横屏", "pc" -> sort = "pc"
                "竖屏", "mp" -> sort = "mp"
                "银发", "yin" -> sort = "yin"
                "兽耳", "cat" -> sort = "cat"
                "星空", "xingkong", "xk" -> sort = "xing"
                "涩图", "setu", "st" -> sort = vip[0]
                "丝袜", "siwa", "sw" -> sort = vip[1]
                "白丝", "baisi", "bs" -> sort = vip[2]
                "黑丝", "heisi", "hs" -> sort = vip[3]
            }
        }

        if (vip.contains(sort)) {
            val apiResult = HttpUtil.getString(vipUrl + sort)
            if (apiResult.isBlank()) return "api获取为空".toPlainText()
            JsonUtil.getArray(apiResult, "pic")[0].asString.run(HttpUtil::getInputStream)
                .use { return subject.uploadImage(it) }
        }
        urls.forEach {
            try {
                val apiResult = HttpUtil.getString(it + sort)
                JsonUtil.getArray(apiResult, "pic")[0].asString.run(HttpUtil::getInputStream)
                    .use { img -> return subject.uploadImage(img) }
            } catch (e: Exception) {
                return@forEach
            }
        }

        return "执行失败".toPlainText()
    }
}
