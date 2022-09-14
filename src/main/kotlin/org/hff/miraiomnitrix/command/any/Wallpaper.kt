package org.hff.miraiomnitrix.command.any

import cn.hutool.json.JSONUtil
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.result.ResultMessage
import org.hff.miraiomnitrix.result.fail
import org.hff.miraiomnitrix.utils.HttpUtil

@Command(name = ["壁纸", "bizhi"])
class Wallpaper : AnyCommand {

    private val urls = listOf(
        "http://api.iw233.cn/api.php?type=json&sort=",
        "http://ap1.iw233.cn/api.php?type=json&sort=",
        "http://skri.iw233.cn/api.php?type=json&sort=",
        "http://aqua.iw233.cn/api.php?type=json&sort=",
        "https://dev.iw233.cn/api.php?type=json&sort=",
        "https://iw233.cn/api.php?type=json&sort="
    )

    //private val stUrl = "https://qiafan.vip/mirlkoi.php?sort=setu"
    private val vipUrl = "https://qiafan.vip/api.php?type=json&sort="
    private val vip = arrayOf("setu", "bs", "hs")

    override suspend fun execute(
        sender: User,
        message: MessageChain,
        subject: Contact,
        args: List<String>
    ): ResultMessage? {
        var sort = "random"
        args.forEach {
            when (it) {
                "正常", "no", "normal" -> sort = "iw233"
                "推荐", "精选", "top" -> sort = "top"
                "横屏", "pc" -> sort = "pc"
                "竖屏", "mp" -> sort = "mp"
                "银发", "yin" -> sort = "yin"
                "兽耳", "cat" -> sort = "cat"
                "星空", "xingkong", "xk" -> sort = "xing"
                "涩图", "setu", "st" -> sort = vip[0]
                "白丝", "baisi", "bs" -> sort = vip[1]
                "黑丝", "heisi", "hs" -> sort = vip[2]
            }
        }

        if (vip.contains(sort)) {
            val response1 = HttpUtil.getString(vipUrl + sort)
            if (response1?.statusCode() != 200) return fail()
            val url = JSONUtil.parseObj(response1.body()).getJSONArray("pic").getStr(0)
            val response2 = HttpUtil.getInputStream(url)
            if (response2?.statusCode() != 200) return fail()
            subject.sendImage(response2.body())
            return null
        }
        for (i in 0..5) {
            val response1 = HttpUtil.getString(urls[i] + sort)
            if (response1?.statusCode() != 200) continue
            val url = JSONUtil.parseObj(response1.body()).getJSONArray("pic").getStr(0)
            val response2 = HttpUtil.getInputStream(url)
            if (response2?.statusCode() != 200) continue
            subject.sendImage(response2.body())
            return null
        }

        return fail()
    }
}