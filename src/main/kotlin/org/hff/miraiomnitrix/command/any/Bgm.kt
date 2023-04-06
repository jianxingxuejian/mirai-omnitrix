package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.buildForwardMessage
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.JsonUtil
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
            val json = HttpUtil.getString(calendarApi)
            val calendarList: List<Calendar> = JsonUtil.fromJson(json)
            val calendar = calendarList[index - 1]
            return buildForwardMessage(subject) {
                calendar.items.forEachLaunch {
                    it.buildCalendar(subject).run(::add)
                }
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
        val searchParam =
            SearchParam(keyword = if (keywords.isEmpty()) null else keywords.joinToString(" "), filter = filter)
        println(searchParam)
        val json = HttpUtil.postString(searchApi, searchParam)
        val result: SearchResult = JsonUtil.fromJson(json)
        return buildForwardMessage(subject) {
            result.data.forEachLaunch { it.buildSearch(context).run(::add) }
        }
    }

    private suspend fun Item.buildCalendar(subject: Contact) =
        buildMessageChain {
            +HttpUtil.getInputStream(images.medium.replaceFirst("http", "https"))
                .use { subject.uploadImage(it) }
            +"\n"
            +"名字: ${name_cn.ifBlank { name }}\n"
            if (summary.isNotBlank()) +"简介: $summary\n"
            +"排名: ${if (rank != 0) rank else "暂无"}\n"
            +"评分: ${rating?.score ?: "暂无"}(${rating?.total ?: 0}人)\n"
            +"链接: $url"
        }

    private suspend fun Data.buildSearch(subject: Contact) = buildMessageChain {
        if (image.isNotBlank()) {
            +HttpUtil.getInputStream(image).use { subject.uploadImage(it) }
            +"\n"
        }
        +"名字: ${name_cn.ifBlank { name }}\n"
        +"排名: ${if (rank != 0) rank else "暂无"}\n"
        +"评分: ${if (score != null && score != 0.0) score else "暂无"}\n"
        if (summary?.isNotBlank() == true) +"简介: $summary\n"
        if (tags.isNotEmpty()) +"标签: ${tags.joinToString { tag -> tag.name }}"
    }

    private fun parseArgs(args: List<String>, i: Int, values: MutableList<String>) {
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
    )

    data class Images(val medium: String)
    data class Rating(val score: Double, val total: Int)

    data class SearchParam(val filter: Filter, val keyword: String?, val sort: String = "rank")
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
    )

    data class Tag(val count: Int, val name: String)
}
