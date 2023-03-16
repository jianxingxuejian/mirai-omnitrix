package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.event.events.MessageEvent
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.CommandResult
import org.hff.miraiomnitrix.command.result
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.JsonUtil
import org.hff.miraiomnitrix.utils.sendImage

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

    override suspend fun execute(args: List<String>, event: MessageEvent): CommandResult? {
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

        val subject = event.subject

        if (vip.contains(sort)) {
            val apiResult = HttpUtil.getString(vipUrl + sort)
            if (apiResult.isBlank()) return result("api获取为空")
            val url = JsonUtil.getArray(apiResult, "pic")[0].asString
            HttpUtil.getInputStream(url).use { subject.sendImage(it) }
            return null
        }
        urls.forEach {
            try {
                val apiResult = HttpUtil.getString(it + sort)
                val url = JsonUtil.getArray(apiResult, "pic")[0].asString
                HttpUtil.getInputStream(url).use {img-> subject.sendImage(img) }
            } catch (e: Exception) {
                return@forEach
            }
            return null
        }

        return result("执行失败")
    }
}
