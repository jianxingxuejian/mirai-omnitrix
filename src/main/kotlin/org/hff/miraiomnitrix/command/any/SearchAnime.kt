package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.common.getImage
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.toTime
import org.hff.miraiomnitrix.utils.uploadImage
import kotlin.math.roundToInt

@Command(name = ["sf", "搜番", "soufan"])
class SearchAnime : AnyCommand {

    override val needHead = false

    private val traceUrl = "https://api.trace.moe/search?url="

    override suspend fun MessageEvent.execute(args: List<String>): Message? {
        val imgUrl = message.getImage()?.queryUrl() ?: return "未找到图片，重新发送图片+关键字重试".toPlainText()
        HttpUtil.getJson<TracemoeResult>(traceUrl + imgUrl).run {
            if (error.isNotBlank()) return At(sender) + error
            with(result[0]) {
                return buildMessageChain {
                    +uploadImage(HttpUtil.getInputStream(image))
                    +"\n相似度：${(similarity * 100).roundToInt()}%"
                    +"\n标题：${filename.substringBefore('.')}"
                    +"\n集数：$episode"
                    +"\n时间：${from.toTime()}"
                }
            }
        }
    }

    private data class TracemoeResult(val error: String, val result: List<Result>)
    private data class Result(
        val anilist: Int,
        val episode: Int,
        val filename: String,
        val from: Double,
        val image: String,
        val similarity: Double,
        val to: Double,
        val video: String
    )

}
