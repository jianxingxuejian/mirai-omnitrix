package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.*
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.add
import org.hff.miraiomnitrix.utils.forEachLaunch
import java.time.LocalDate

/** [api文档地址](https://bangumi.github.io/api) */
@Command(name = ["番剧推荐", "bgm"])
class Bgm : AnyCommand {

    private val calendarApi = "https://api.bgm.tv/calendar"
    private val searchApi = "https://api.bgm.tv/v0/search/subjects?limit=10"

    private val help = """
        |使用放送、day等关键字可获取bgm的每日放送列表，可追加1-7选择星期
        |可以用关键字以及查询条件搜索番剧，查询条件有日期、评分、排名、标签
        |示例1：摇曳露营
        |示例2：轻音少女 评分 >6 排名 <=5000
        |示例3：全金属狂潮 日期 >2008 <2020 评分 >=4.5 排名 <3000 >100 标签 冒险
    """.trimMargin().toPlainText()

    private val calendar = hashSetOf("每日放送", "每日", "放送", "放送表", "calendar", "day")

    override suspend fun MessageEvent.execute(args: List<String>): Message? {
        if (args.isEmpty() || args[0] == "help") return help

        if (calendar.contains(args[0])) {
            val index = args.getOrNull(1)?.toIntOrNull() ?: LocalDate.now().dayOfWeek.value
            val calendarList: List<Calendar> = HttpUtil.getJson(calendarApi)
            val calendar = calendarList[index - 1]
            return buildForwardMessage(subject) {
                calendar.items.forEachLaunch { it.buildCalendar(subject).let(::add) }
            }
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
                else -> if (listOf(airDate, rank, rating, tag).none { args[i] in it }) keywords.add(args[i])
            }
        }
        val filter = Filter(air_date = airDate, rating = rating, rank = rank, tag = tag)
        val keyword = if (keywords.isEmpty()) null else keywords.joinToString(" ")
        val searchParam = SearchParam(keyword = keyword, filter = filter)
        val result: SearchResult = HttpUtil.postJson(searchApi, searchParam)
        return buildForwardMessage(subject) {
            result.data.forEachLaunch { it.buildSearch(subject).let(::add) }
        }
    }

    private suspend fun Item.buildCalendar(subject: Contact): MessageChain = buildMessageChain {
        images?.medium?.replaceFirst("http", "https")?.let { url ->
            +HttpUtil.getInputStream(url).use { subject.uploadImage(it) }
        }
        +"\n名字: ${name_cn.ifBlank { name }}"
        if (summary.isNotBlank()) +"\n简介: $summary"
        +"\n排名: ${if (rank != 0) rank else "暂无"}"
        +"\n评分: ${rating?.score ?: "暂无"}(${rating?.total ?: 0}人)"
        +"\n链接: $url"
    }


    private suspend fun Data.buildSearch(subject: Contact) = buildMessageChain {
        if (image.isNotBlank()) +HttpUtil.getInputStream(image).use { subject.uploadImage(it) }
        +"\n名字: ${name_cn.ifBlank { name }}"
        +"\n排名: ${if (rank != 0) rank else "暂无"}"
        +"\n评分: ${if (score != null && score != 0.0) score else "暂无"}"
        if (summary?.isNotBlank() == true) +"\n简介: $summary"
        if (tags.isNotEmpty()) +"\n标签: ${tags.joinToString { tag -> tag.name }}"
    }

    private fun parseArgs(args: List<String>, i: Int, values: MutableList<String>) {
        if (i < args.size - 1 && (args[i + 1][0] == '<' || args[i + 1][0] == '>')) {
            values.add(args[i + 1])
        }
        if (i < args.size - 2 && (args[i + 2][0] == '<' || args[i + 2][0] == '>')) {
            values.add(args[i + 2])
        }
    }

    private data class Calendar(val items: List<Item>)
    private data class Item(
        val url: String,
        val name: String,
        val name_cn: String,
        val rank: Int,
        val rating: Rating?,
        val summary: String,
        val images: Images?,
    )

    private data class Images(val medium: String)
    private data class Rating(val score: Double, val total: Int)

    private data class SearchParam(val filter: Filter, val keyword: String?, val sort: String = "rank")
    private data class Filter(
        val air_date: List<String>,
        val rank: List<String>,
        val rating: List<String>,
        val tag: List<String>,
        val type: List<Int> = listOf(2)
    )

    private data class SearchResult(val `data`: List<Data>, val limit: Int, val offset: Int, val total: Int)
    private data class Data(
        val name: String,
        val name_cn: String,
        val rank: Int,
        val score: Double?,
        val summary: String?,
        val image: String,
        val tags: List<Tag>,
    )

    private data class Tag(val count: Int, val name: String)

}
