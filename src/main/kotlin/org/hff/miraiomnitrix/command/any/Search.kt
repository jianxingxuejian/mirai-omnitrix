package org.hff.miraiomnitrix.command.any

import cn.hutool.json.JSONUtil
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChainBuilder
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.result.ResultMessage
import org.hff.miraiomnitrix.result.fail
import org.hff.miraiomnitrix.result.result
import org.hff.miraiomnitrix.utils.HttpUtil

@Command(name = ["搜图", "soutu", "st"], isNeedHeader = false)
class Search : AnyCommand {

    private val url = "https://saucenao.com/search.php?db=999&output_type=2&numres=1&api_key="
    private val key = "b34d8184b408b14507bff4ba076b5706877d4c53"

    override suspend fun execute(
        sender: User,
        message: MessageChain,
        subject: Contact,
        args: List<String>
    ): ResultMessage? {
        val image = message[Image.Key] ?: return result("请发送一张图片")

        val response1 = HttpUtil.getStringByProxy("$url$key&url=${image.queryUrl()}")
        if (response1?.statusCode() != 200) return fail()

        val array = JSONUtil.parseObj(response1.body()).getJSONArray("results")
        if (array.size < 1) return fail()
        val result = array.getJSONObject(0)
        val header = result.getJSONObject("header")
        val data = result.getJSONObject("data")
        val thumbnail = header.getStr("thumbnail")
        val response2 = HttpUtil.getInputStreamByProxy(thumbnail)
        if (response2?.statusCode() != 200) return fail()

        val builder = MessageChainBuilder()
        builder.append("搜图结果：\n")
            .append(subject.uploadImage(response2.body()))
            .append("相似度：").append(header.getStr("similarity")).append("\n")
            .append("标题：").append(data.getStr("title")).append("\n")
            .append("链接：").append(data.getStr("ext_urls")).append("\n")
            .append("作者：").append(data.getStr("member_name")).append("\n")
        return result(builder.build())
    }
}