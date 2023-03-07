package org.hff.miraiomnitrix.command.any

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.buildForwardMessage
import net.mamoe.mirai.message.data.buildMessageChain
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.CommandResult
import org.hff.miraiomnitrix.command.result
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.JsonUtil
import java.time.LocalDate

@Command(name = ["番剧推荐", "bgm"])
class Bgm : AnyCommand {

    private val calendarApi = "https://api.bgm.tv/calendar"
    private val searchApi = "https://api.bgm.tv/v0/search/subjects?limit=10"

    override suspend fun execute(args: List<String>, event: MessageEvent): CommandResult? {
        if (args.isEmpty()) return result("")
        val subject = event.subject

        if (listOf("每日放送", "每日", "放送", "放送表", "calendar", "day").contains(args[0])) {
            val index = args.getOrNull(1)?.toIntOrNull() ?: LocalDate.now().dayOfWeek.value
            val json = HttpUtil.getString(calendarApi)
            val calendarList: List<Calendar> = JsonUtil.fromJson(json)
            val calendar = calendarList[index - 1]
            buildForwardMessage(subject) {
                coroutineScope {
                    calendar.items.forEach {
                        launch {
                            val image = HttpUtil.getInputStream(it.images.medium.replaceFirst("http", "https"))
                            val message = buildMessageChain {
                                +subject.uploadImage(image)
                                +"\n"
                                +"名字: ${it.name_cn.ifBlank { it.name }}\n"
                                if (it.rank != 0) +"排名: ${it.rank}\n"
                                if (it.summary.isNotBlank()) +"简介: ${it.summary}\n"
                                if (it.rating != null) +"评分: ${it.rating.score}(${it.rating.total}人)\n"
                            }
                            add(subject.bot, message)
                        }
                    }
                }
            }.apply { subject.sendMessage(this) }
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
        buildForwardMessage(subject) {
            coroutineScope {
                result.data.forEach {
                    launch {
                        val message = buildMessageChain {
                            if (it.image.isNotBlank()) {
                                +subject.uploadImage(HttpUtil.getInputStream(it.image))
                                +"\n"
                            }
                            +"名字: ${it.name_cn.ifBlank { it.name }}\n"
                            if (it.rank != 0) +"排名: ${it.rank}\n"
                            if (it.score != null && it.score != 0.0) +"评分: ${it.score}\n"
                            if (it.summary?.isNotBlank() == true) +"简介: ${it.summary}\n"
                            +"标签: ${it.tags.joinToString { tag -> tag.name }}"
                        }
                        add(subject.bot, message)
                    }
                }
            }
        }.apply { subject.sendMessage(this) }
        return null
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
        val air_date: String,
        val air_weekday: Int,
        val images: Images,
        val name: String,
        val name_cn: String,
        val rank: Int,
        val rating: Rating?,
        val summary: String,
        val url: String
    )

    data class Images(
        val common: String,
        val grid: String,
        val large: String,
        val medium: String,
        val small: String
    )

    data class Rating(
        val score: Double,
        val total: Int
    )

    data class SearchParam(
        val filter: Filter,
        val keyword: String,
        val sort: String = "rank"
    )

    data class Filter(
        val air_date: List<String>? = null,
        val rank: List<String>? = null,
        val rating: List<String>? = null,
        val tag: List<String>? = null,
        val type: List<Int> = listOf(2)
    )

    data class SearchResult(
        val `data`: List<Data>,
        val limit: Int,
        val offset: Int,
        val total: Int
    )

    data class Data(
        val date: String,
        val id: Int,
        val image: String,
        val name: String,
        val name_cn: String,
        val rank: Int,
        val score: Double?,
        val summary: String?,
        val tags: List<Tag>,
        val type: Int
    )

    data class Tag(
        val count: Int,
        val name: String
    )
}