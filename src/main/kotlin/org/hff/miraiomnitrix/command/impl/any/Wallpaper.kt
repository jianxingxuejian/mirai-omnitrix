package org.hff.miraiomnitrix.command.impl.any

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.command.core.Command
import org.hff.miraiomnitrix.command.type.AnyCommand
import org.hff.miraiomnitrix.result.ResultMessage
import org.hff.miraiomnitrix.result.fail
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.ImageUtil.imageCache
import org.hff.miraiomnitrix.utils.JsonUtil
import java.io.InputStream

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

    override suspend fun execute(
        sender: User,
        message: MessageChain,
        subject: Contact,
        args: List<String>,
        event: MessageEvent
    ): ResultMessage? {
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
            if (apiResult.isBlank()) return fail()
            val url = JsonUtil.getArray(apiResult, "pic")[0].asString
            val image = HttpUtil.getInputStream(url)
            sendImage(subject, image)
            return null
        }
        urls.forEach {
            try {
                val apiResult = HttpUtil.getString(it + sort)
                val url = JsonUtil.getArray(apiResult, "pic")[0].asString
                val img = HttpUtil.getInputStream(url)
                sendImage(subject, img)
            } catch (e: Exception) {
                return@forEach
            }
            return null
        }

        return fail()
    }

    suspend fun sendImage(subject: Contact, inputStream: InputStream) {
        val image = subject.uploadImage(inputStream)
        val send = subject.sendMessage(image)
        imageCache.put(send.source.internalIds[0], image.imageId)
    }
}