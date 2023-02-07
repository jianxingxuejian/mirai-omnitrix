package org.hff.miraiomnitrix.command.any

import kotlinx.coroutines.*
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.nextMessage
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.config.AccountProperties
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.exception.MyException
import org.hff.miraiomnitrix.result.CommandResult
import org.hff.miraiomnitrix.result.CommandResult.Companion.result
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.JsonUtil
import org.hff.miraiomnitrix.utils.JsonUtil.getAsStr
import java.time.LocalDate

@Command(["chat", "聊天"])
class Chat(accountProperties: AccountProperties, permissionProperties: PermissionProperties) : AnyCommand {

    private val chatIncludeGroup = permissionProperties.chatIncludeGroup
    private val stateCache = mutableMapOf<Long, MutableSet<Long>>()
    private val apiKey = accountProperties.openaiApiKey?.let { "Bearer $it" }
    private var model = "text-chat-davinci-002-20221122"
    private var maxTokens = 3200
    private var temperature = 0.5

    override suspend fun execute(
        sender: User,
        message: MessageChain,
        subject: Contact,
        args: List<String>,
        event: MessageEvent
    ): CommandResult? {
        if (subject is Group && chatIncludeGroup.isNotEmpty()) {
            if (!chatIncludeGroup.contains(subject.id)) return null
        }

        val cache = stateCache[sender.id]
        if (cache == null) stateCache[sender.id] = mutableSetOf(subject.id)
        else if (cache.contains(subject.id)) return result("问答线程已经开始，请@我并说出你的问题")

        val name = sender.nameCardOrNick
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val buffer =
                    StringBuffer("$name: 你是ChatGPT, 一个由OpenAI训练的大型语言模型。现在的时间是:${LocalDate.now()}，开始问答式交流。")
                if (args.isEmpty()) subject.sendMessage("你好，我是ChatGPT，现在开始问答，请@我并说出你的问题")
                else {
                    buffer.append("${args.joinToString("\n")}\n\n")
                    val reply = completion(buffer, name)
                    subject.sendMessage(At(sender) + reply)
                }
                while (isActive) {
                    val next = event.nextMessage(300_000L, EventPriority.HIGH, intercept = true)
                    val content = next.contentToString()
                    if (arrayOf("退出", "结束", "停止", "exit", "stop").contains(content)) {
                        subject.sendMessage(At(sender) + "本次聊天已结束")
                        break
                    }
                    val at = "@" + event.bot.id
                    if (!content.startsWith(at)) continue
                    buffer.append("$name：${content.replace(at, "")}\n\n")
                    val reply = completion(buffer, sender.nameCardOrNick)
                    subject.sendMessage(message.quote() + reply)
                }
            }
        } catch (_: TimeoutCancellationException) {
            subject.sendMessage(At(event.sender.id) + "超时，问答已结束")
        } finally {
            stateCache[sender.id]?.remove(subject.id)
        }

        return null
    }

    fun completion(buffer: StringBuffer, user: String): String {
        if (apiKey == null) throw MyException("未配置apikey")
        val headers = mapOf("Authorization" to apiKey)
        val params = mapOf(
            "model" to model,
            "prompt" to buffer.toString(),
            "max_tokens" to maxTokens,
            "temperature" to temperature
        )
        val json = HttpUtil.postStringByProxy("https://api.openai.com/v1/completions", params, headers)
        val choices = JsonUtil.getArray(json, "choices")
        if (choices.isEmpty) throw MyException("choices为空")
        val text = choices[0].getAsStr("text").removePrefix("\n\n").replace("<|im_end|>", "")
        buffer.append("$text\n\n")
        return text.replace("ChatGPT:", "").trim().split("\n\n").joinToString("\n")
    }

}