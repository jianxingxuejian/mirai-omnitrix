package org.hff.miraiomnitrix.command.any

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.ForwardMessageBuilder
import net.mamoe.mirai.message.data.buildMessageChain
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.CommandResult
import org.hff.miraiomnitrix.command.result
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.JsonUtil
import org.hff.miraiomnitrix.utils.add
import java.time.LocalDate

@Command(name = ["番剧推荐", "bgm"])
class Bgm : AnyCommand {

    private val calendarApi = "https://api.bgm.tv/calendar"
    private val searchApi = "https://api.bgm.tv/v0/search/subjects?limit=10"

    private val text = """
        |使用放送、day等关键字可获取bgm的每日放送列表，可追加1-7选择星期
        |可以用关键字以及查询条件搜索番剧，查询条件有日期、评分、排名、标签
        |示例1：摇曳露营
        |示例2：轻音少女 评分 >6 排名 <=5000
        |示例3：全金属狂潮 日期 >2008 <2020 评分 >=4.5 排名 <3000 >100 标签 冒险
    """.trimMargin()
    private val help = result(text)

    override suspend fun execute(args: List<String>, event: MessageEvent): CommandResult? {
        if (args.isEmpty()) return help
        if (args[0] == "help") return help
        val subject = event.subject

        if (hashSetOf("每日放送", "每日", "放送", "放送表", "calendar", "day").contains(args[0])) {
            val index = args.getOrNull(1)?.toIntOrNull() ?: LocalDate.now().dayOfWeek.value
            val json = HttpUtil.getString(calendarApi)
            val calendarList: List<Calendar> = JsonUtil.fromJson(json)
            val calendar = calendarList[index - 1]
            val forward = ForwardMessageBuilder(subject)
            coroutineScope {
                calendar.items.forEach { (url, name, name_cn, rank, rating, summary, images) ->
                    launch {
                        val image = HttpUtil.getInputStream(images.medium.replaceFirst("http", "https"))
                            .use { subject.uploadImage(it) }
                        buildMessageChain {
                            +image
                            +"\n"
                            +"名字: ${name_cn.ifBlank { name }}\n"
                            if (rank != 0) +"排名: $rank\n" else +"暂无排名\n"
                            if (summary.isNotBlank()) +"简介: $summary\n"
                            +"评分: ${rating?.score ?: "暂无评分"}(${rating?.total ?: 0}人)\n"
                            +"链接: $url"
                        }.run(forward::add)
                    }
                }
            }
            return result(forward)
        }

        val airDate = mutableListOf<String>()
        val rank = mutableListOf<String>()
        val rating = mutableListOf<String>()
        val tag = mutableListOf<String>()
        val keywords = mutableListOf<String>()

        for (i in args.indices) {
            when (args[i]) {
                "日期" -> parseArgs(args, i, airDate)
                "评分" -> parseArgs(args, i, rating)
                "排名" -> parseArgs(args, i, rank)
                "标签" -> parseArgs(args, i, tag)
                else -> keywords.add(args[i])
            }
        }
        val filter = Filter(air_date = airDate, rating = rating, rank = rank, tag = tag)
        val searchParam = SearchParam(keyword = keywords.joinToString(" "), filter = filter)
        val json = HttpUtil.postString(searchApi, searchParam)
        val result: SearchResult = JsonUtil.fromJson(json)
        val forward = ForwardMessageBuilder(subject)
        coroutineScope {
            result.data.forEach { (name, name_cn, rank, score, summary, image, tags) ->
                launch {
                    buildMessageChain {
                        if (image.isNotBlank()) {
                            +HttpUtil.getInputStream(image).use { subject.uploadImage(it) }
                            +"\n"
                        }
                        +"名字: ${name_cn.ifBlank { name }}\n"
                        if (rank != 0) +"排名: $rank\n" else +"暂无排名\n"
                        +"评分: ${if (score != null && score != 0.0) score else "暂无评分"}\n"
                        if (summary?.isNotBlank() == true) +"简介: $summary\n"
                        if (tags.isNotEmpty()) +"标签: ${tags.joinToString { tag -> tag.name }}"
                    }.run(forward::add)
                }
            }
        }
        return result(forward)
    }

    fun parseArgs(args: List<String>, i: Int, values: MutableList<String>) {
        if (i < args.size - 1 && (args[i + 1][0] == '<' || args[i + 1][0] == '>')) {
            values.add(args[i + 1])
        }
        if (i < args.size - 2 && (args[i + 2][0] == '<' || args[i + 2][0] == '>')) {
            values.add(args[i + 2])
        }
    }

    data class Calendar(val items: List<Item>)

    data class Item(
        val url: String,
        val name: String,
        val name_cn: String,
        val rank: Int,
        val rating: Rating?,
        val summary: String,
        val images: Images,
        val air_date: String,
        val air_weekday: Int,
    )

    data class Images(
        val common: String,
        val grid: String,
        val large: String,
        val medium: String,
        val small: String
    )

    data class Rating(val score: Double, val total: Int)

    data class SearchParam(val filter: Filter, val keyword: String, val sort: String = "rank")

    data class Filter(
        val air_date: List<String>,
        val rank: List<String>,
        val rating: List<String>,
        val tag: List<String>,
        val type: List<Int> = listOf(2)
    )

    data class SearchResult(val `data`: List<Data>, val limit: Int, val offset: Int, val total: Int)

    data class Data(
        val name: String,
        val name_cn: String,
        val rank: Int,
        val score: Double?,
        val summary: String?,
        val image: String,
        val tags: List<Tag>,
        val date: String,
        val type: Int
    )

    data class Tag(val count: Int, val name: String)
}
