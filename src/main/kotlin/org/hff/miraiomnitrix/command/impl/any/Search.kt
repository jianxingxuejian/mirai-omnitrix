package org.hff.miraiomnitrix.command.impl.any

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.QuoteReply
import org.hff.miraiomnitrix.command.core.Command
import org.hff.miraiomnitrix.command.type.AnyCommand
import org.hff.miraiomnitrix.config.AccountProperties
import org.hff.miraiomnitrix.result.ResultMessage
import org.hff.miraiomnitrix.result.fail
import org.hff.miraiomnitrix.result.result
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.ImageUtil.imageCache
import org.hff.miraiomnitrix.utils.JsonUtil
import org.hff.miraiomnitrix.utils.JsonUtil.getAsStr
import org.hff.miraiomnitrix.utils.JsonUtil.getAsStrOrNull

@Command(name = ["搜图", "soutu", "st"])
class Search(accountProperties: AccountProperties) : AnyCommand {

    private val url = "https://saucenao.com/search.php?db=999&output_type=2&numres=1&api_key="
    private val key = accountProperties.saucenaoKey

    override suspend fun execute(
        sender: User,
        message: MessageChain,
        subject: Contact,
        args: List<String>
    ): ResultMessage? {
        if (key == null) return result("请配置saucenao api的key")

        val quote = message[QuoteReply.Key]
        val image = if (quote != null) {
            val imageId = imageCache.getIfPresent(quote.source.internalIds[0]) ?: return result("请发送一张图片")
            Image(imageId)
        } else {
            message[Image.Key] ?: return result("请发送一张图片")
        }

        val apiResult = HttpUtil.getStringByProxy("$url$key&url=${image.queryUrl()}")
        val array = JsonUtil.getArray(apiResult, "results")
        if (array.isEmpty) return fail()
        val result = array[0].asJsonObject
        val header = result.getAsJsonObject("header")
        val data = result.getAsJsonObject("data")
        val thumbnail = header.getAsStr("thumbnail")
        val thumbnailImg = HttpUtil.getInputStreamByProxy(thumbnail)
        val urls = data.getAsJsonArray("ext_urls")
        val urlsText = if (urls.size() == 1) {
            "链接: " + urls[0] + "\n"
        } else {
            urls.mapIndexed { index, url -> "链接${index + 1}：$url\n" }.joinToString("")
        }
        val builder = MessageChainBuilder()
        builder.append("搜图结果：\n")
            .append(subject.uploadImage(thumbnailImg))
            .append("相似度：").append(header.getAsStr("similarity")).append("\n")
            .append("标题：").append(data.getAsStrOrNull("title")).append("\n")
            .append(urlsText)
            .append("作者：").append(
                data.getAsStrOrNull("member_name") ?: data.getAsStrOrNull("user_name")
                ?: data.getAsStrOrNull("creator") ?: data.getAsStrOrNull("jp_name")
            )
            .append("\n")
        return result(builder.build())
    }
}