package org.hff.miraiomnitrix.event.group

import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import org.hff.miraiomnitrix.config.AccountProperties
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.event.*
import org.hff.miraiomnitrix.exception.MyException
import org.hff.miraiomnitrix.utils.*
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@Event(priority = 5)
class Imitate(
    accountProperties: AccountProperties,
    private val permissionProperties: PermissionProperties
) : GroupEvent {

    private val apiKey = accountProperties.openaiApiKey?.let { "Bearer $it" }
    private var maxTokens = 1000
    private var temperature = 0.1
    val promptA = """
        |这是一场模仿实验，你要在一个虚假的聊天室中扮演一个真实的人，你的任务是根据聊天记录进行随机发言，不限制内容，但是要切合聊天记录。
        |以下是历史对话记录：%s。
        |接下来你判断有没有必要进行发言，发言的内容是什么,不过尽量不要发言以免暴露自己。
        |你的回复格式必须要遵循如下规则：第一行只能回复yes或者no，代表你是否要发言，如果你认为可以发言，则回复yes，第二行是回复的内容，不打算发言只需要回复no。
    """.trimMargin()
    val promptB = """
        |你的工作是阅读一个虚拟聊天室的聊天记录，尽可能浓缩对话文本，保留聊天上下文中有用的信息，其余抛弃。
        |每一个人的发言用`userA:谈话内容`的格式，单独一行以换行符结尾。
        |以下是历史对话记录：%s。
        |请你回复浓缩后的对话记录，不能超过50条。
    """.trimMargin()

    val historyRecord = hashMapOf<Long, MutableList<String>>()
    val tempRecord = hashMapOf<Long, MutableList<String>>()

    override suspend fun handle(args: List<String>, event: GroupMessageEvent, isAt: Boolean): EventResult {
        val (group, sender, message) = event.getInfo()
        if (!permissionProperties.imitateIncludeGroup.contains(group.id)) return next()
        val text = message.contentToString()
        if (text.length > 100) return stop()
        val temp = tempRecord.getOrPut(group.id) { mutableListOf() }
            .apply { add("${sender.nameCardOrNick}：${text}") }
        if (temp.size > 9) {
            var history = historyRecord.getOrPut(group.id) { mutableListOf() }.apply { addAll(temp) }
            temp.clear()
            try {
                if (history.size > 60) history = completion(promptB, history).split("\n").toMutableList()
                val content = completion(promptA, history).split("\n")
                println(history)
                println(content)
                if (content[0] == "yes") {
                    val talk = content.drop(1).joinToString("\n")
                    history.add("自己：$talk")
                    return stop(talk)
                }
            } catch (_: Exception) {
                history.clear()
                temp.clear()
                return stop()
            }
            return stop()
        }
        return stop()
    }

    fun completion(prompt: String, history: MutableList<String>): String {
        if (apiKey == null) throw MyException("未配置apikey")
        val headers = mapOf("Authorization" to apiKey)
        val content = prompt.format(history.joinToString("\n"))
        val message = mapOf("role" to "user", "content" to content)
        val params = mapOf(
            "model" to "gpt-3.5-turbo-0301",
            "messages" to listOf(message),
            "max_tokens" to maxTokens,
            "temperature" to temperature
        )
        val json = HttpUtil.postStringByProxy("https://api.openai.com/v1/chat/completions", params, headers)
        val choices = JsonUtil.getArray(json, "choices")
        if (choices.isEmpty) throw MyException("choices为空")
        return choices[0].get("message").getAsStr("content").removePrefix("\n\n")
    }
}
