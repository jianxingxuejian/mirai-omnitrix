package org.hff.miraiomnitrix.event.group

import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import org.hff.miraiomnitrix.config.AccountProperties
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.event.*
import org.hff.miraiomnitrix.exception.MyException
import org.hff.miraiomnitrix.utils.*

@Event(priority = 5)
class Imitate(
    accountProperties: AccountProperties,
    private val permissionProperties: PermissionProperties
) : GroupEvent {

    private val apiKey = accountProperties.openaiApiKey?.let { "Bearer $it" }
    private var maxTokens = 1000
    private var temperature = 0.1
    val promptA = """
        |你是一个喜欢在网络上与其他人交流的AI，你加入了一个网络聊天室已经过了一段时间，为了融入他们你需要尽量模仿他们的发言。
        |你的任务是根据历史聊天记录进行发言，不限制内容，但是要切合聊天记录，不要发出没有意义的内容，例如打招呼、附和别人等。
        |以下是历史对话记录：%s。
        |历史记录中的"自己："开头的是你过去的发言记录，请记住不要重复过去说过的话。
        |接下来你判断有没有必要进行发言，发言的内容是什么,尽量控制发言的频次。
        |发言的内容不要太过正式，可以参照其他人的口吻和语气以增加真实度。
        |你的回复格式必须要遵循如下规则：第一行只能回复yes或者no，代表你是否要发言，如果你认为可以发言，则回复yes，第二行是回复的内容，不打算发言只需要回复no。
    """.trimMargin()
    val promptB = """
        |你的工作是阅读一个虚拟聊天室的聊天记录，尽可能浓缩对话文本，保留聊天上下文中有用的信息，其余抛弃。
        |每一个人的发言用`userA:谈话内容`的格式，单独一行以换行符结尾。
        |以下是历史对话记录：%s。
        |越前面的记录越不重要，请你回复浓缩后的对话记录，不能超过50条。
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
            }
        }
        return stop()
    }

    fun completion(prompt: String, history: MutableList<String>): String {
        if (apiKey == null) throw MyException("未配置apikey")
        val headers = mapOf("Authorization" to apiKey)
        val content = prompt.format(history.joinToString("\n"))
        val message = mapOf("role" to "user", "content" to content)
        val params = mapOf(
            "model" to "gpt-3.5-turbo",
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
