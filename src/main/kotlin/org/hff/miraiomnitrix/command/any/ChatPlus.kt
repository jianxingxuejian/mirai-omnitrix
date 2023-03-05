package org.hff.miraiomnitrix.command.any

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.nextMessage
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.CommandResult
import org.hff.miraiomnitrix.config.AccountProperties
import org.hff.miraiomnitrix.exception.MyException
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.JsonUtil
import org.hff.miraiomnitrix.utils.JsonUtil.get
import org.hff.miraiomnitrix.utils.JsonUtil.getAsStr
import org.hff.miraiomnitrix.utils.getInfo
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate

@Command(["chatplus", "高级聊天"])
class ChatPlus(accountProperties: AccountProperties) : AnyCommand {

    val prompt =
        """你是ChatGPT-Plus，一个由OpenAI训练的大型语言模型，当前时间是：%s，接下来你根据我的指令进行回答。
                |你有1个外部api，分别是1.Bing搜索api(当你需要联网获取资料时使用这个api，参数为关键字)；
                |每一次的回答你都可以决定是否调用外部api，由你决定调用的参数是什么，调用之前请检查之前是否使用用相同参数进行调用了，请勿使用相同参数进行重复调用。
                |你的回复格式要遵循如下规则：第一行只能回复yes或者no，代表你是否要调用外部api，如果你认为可以开始回答问题了，则回复no，然后回答问题；
                |如果你选择了yes，则第二行回答api的序号，第三行回答api的输入参数，然后回答结束，不要进行多余的回答。
                |我的问题是:
            """.trimMargin()
    private val apiKey = accountProperties.openaiApiKey?.let { "Bearer $it" }
    private var maxTokens = 1000
    private var temperature = 0.1

    private val bingSearchKey = accountProperties.bingSearchKey

    private val stateCache = mutableMapOf<Long, MutableSet<Long>>()

    override suspend fun execute(args: List<String>, event: MessageEvent): CommandResult? {
        val (subject, sender) = event.getInfo()
        val cache = stateCache[sender.id]
        if (cache == null) stateCache[sender.id] = mutableSetOf(subject.id)
        else {
            if (cache.contains(subject.id)) return null
            else stateCache[sender.id]?.add(subject.id)
        }

        val name = sender.nameCardOrNick
        try {
            coroutineScope {
                val buffer = StringBuffer(prompt.format(LocalDate.now()))
                if (args.isEmpty()) {
                    subject.sendMessage(name + "你好，我是ChatGPT-Plus，请@我并说出你的问题")
                } else {
                    subject.sendMessage(name + "你好，我是ChatGPT-Plus，问答即将开始")
                    buffer.append(args.joinToString("\n"))
                    val reply = handleExternalApi(buffer, subject)
                    subject.sendMessage(At(sender) + reply)
                }
                while (isActive) {
                    val next = event.nextMessage(300_000L, EventPriority.HIGH, intercept = true)
                    var content = next.contentToString()
                    if (arrayOf("退出", "结束", "停止", "exit", "stop").contains(content)) {
                        subject.sendMessage(At(sender) + "本次聊天已结束")
                        break
                    }
                    val at = "@" + event.bot.id
                    val isAt = content.startsWith(at)
                    content = if (isAt) {
                        content.replace(at, "")
                    } else {
                        val isChat = content.length > 4 && content.slice(1..4) == "chat"
                        if (!isChat) continue
                        if (content.length <= 5) {
                            subject.sendMessage(At(sender) + "请输入")
                            continue
                        } else content.substring(5)
                    }
                    val temp = buffer.length
                    buffer.append("\n\n${content.trim()}")
                    try {
                        val reply = handleExternalApi(buffer, subject)
                        println(buffer)
                        subject.sendMessage(next.quote() + reply)
                    } catch (_: Exception) {
                        buffer.setLength(temp)
                        subject.sendMessage(At(event.sender.id) + "出现错误，请重试")
                    }
                }
            }
        } catch (_: TimeoutCancellationException) {
            subject.sendMessage(At(event.sender.id) + "超时，问答已结束")
        } catch (e: Exception) {
            subject.sendMessage(At(event.sender.id) + "出现错误，请重试")
            e.printStackTrace()
        } finally {
            stateCache[sender.id]?.remove(subject.id)
        }

        return null
    }

    suspend fun handleExternalApi(buffer: StringBuffer, subject: Contact): String {
        val reply = completion(buffer)
        buffer.append("\n\n$reply")
        val lines = reply.split("\n")
        if (lines.size < 3) return reply
        if (lines[0] == "no") return lines.drop(1).joinToString("\n")
        if (lines[0] == "yes" && lines[1] == "1") {
            search(buffer, lines[2], subject)
            return handleExternalApi(buffer, subject)
        }
        return reply
    }

    suspend fun completion(buffer: StringBuffer): String {
        if (apiKey == null) throw MyException("未配置apikey")
        val headers = mapOf("Authorization" to apiKey)
        val message = mapOf("role" to "user", "content" to buffer.toString())
        val params = mapOf(
            "model" to "gpt-3.5-turbo",
            "messages" to listOf(message),
            "max_tokens" to maxTokens,
            "temperature" to temperature
        )
        val json = HttpUtil.postStringByProxy("https://api.openai.com/v1/chat/completions", params, headers)
        val choices = JsonUtil.getArray(json, "choices")
        if (choices.isEmpty) throw MyException("choices为空")
        val text = choices[0].get("message").getAsStr("content").removePrefix("\n\n")
        buffer.append("\n\n$text")
        return text
    }

    suspend fun search(buffer: StringBuffer, keyword: String, subject: Contact) {
        if (bingSearchKey == null) throw MyException("未配置Bing搜索key")
        val headers = mapOf("Ocp-Apim-Subscription-Key" to bingSearchKey)
        subject.sendMessage("正在调用Bing Api搜索${keyword}的相关内容")
        val json =
            HttpUtil.getString(
                "https://api.bing.microsoft.com/v7.0/search?q=${
                    URLEncoder.encode(keyword, StandardCharsets.UTF_8)
                }&responseFilter=Webpages&count=3", headers
            )
        val result: BingWebSearch = JsonUtil.fromJson(json)
        val values = result.webPages?.value ?: return
        val jsonArray = JsonArray()

        for (value in values) {
            val innerJson = JsonObject()
            innerJson.addProperty("name", value.name)
            innerJson.addProperty("snippet", value.snippet)
            jsonArray.add(innerJson)
        }
        buffer.append("\n\n通过调用Bing搜索api返回如下结果:$jsonArray")
    }

    data class BingWebSearch(
        val _type: String,
        val queryContext: QueryContext,
        val webPages: WebPages?
    )

    data class QueryContext(
        val originalQuery: String
    )

    data class WebPages(
        val totalEstimatedMatches: Int,
        val value: List<ValueX>,
        val webSearchUrl: String
    )

    data class ValueX(
        val dateLastCrawled: String,
        val displayUrl: String,
        val id: String,
        val isFamilyFriendly: Boolean,
        val isNavigational: Boolean,
        val language: String,
        val name: String,
        val snippet: String,
        val url: String
    )

}