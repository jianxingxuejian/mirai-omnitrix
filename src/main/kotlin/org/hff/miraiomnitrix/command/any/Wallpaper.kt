package org.hff.miraiomnitrix.command.any

import cn.hutool.json.JSONUtil
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.listener.AnyListener.imageCache
import org.hff.miraiomnitrix.result.ResultMessage
import org.hff.miraiomnitrix.result.fail
import org.hff.miraiomnitrix.utils.HttpUtil
import java.io.InputStream

@Command(name = ["壁纸", "bizhi"])
class Wallpaper : AnyCommand {

    private val urls = listOf(
//        "http://api.iw233.cn/api.php?type=json&sort=",
//        "http://ap1.iw233.cn/api.php?type=json&sort=",
        "https://dev.iw233.cn/api.php?type=json&sort=",
        "https://iw233.cn/api.php?type=json&sort="
    )

    private val vipUrl = "http://api.iw233.cn/zhuanfasima.php?type=json&sort="
    private val vip = arrayOf("st", "sw", "swbs", "swhs")

    override suspend fun execute(
        sender: User,
        message: MessageChain,
        subject: Contact,
        args: List<String>
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
            val response1 = HttpUtil.getString(vipUrl + sort)
            if (response1?.statusCode() != 200) return fail()
            val url = JSONUtil.parseObj(response1.body()).getJSONArray("pic").getStr(0)
            val response2 = HttpUtil.getInputStream(url)
            if (response2?.statusCode() != 200) return fail()
            sendImage(subject, response2.body())
            return null
        }
        for (i in 0..5) {
            val response1 = HttpUtil.getString(urls[i] + sort)
            if (response1?.statusCode() != 200) continue
            val url = JSONUtil.parseObj(response1.body()).getJSONArray("pic").getStr(0)
            val response2 = HttpUtil.getInputStream(url)
            if (response2?.statusCode() != 200) continue
            sendImage(subject, response2.body())
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