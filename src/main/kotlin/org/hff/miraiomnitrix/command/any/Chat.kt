package org.hff.miraiomnitrix.command.any

import kotlinx.coroutines.*
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.message.nextMessage
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.config.AccountProperties
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.exception.MyException
import org.hff.miraiomnitrix.utils.*
import java.time.LocalDate

@Command(["chat", "聊天"])
class Chat(accountProperties: AccountProperties, private val permissionProperties: PermissionProperties) : AnyCommand {

    private val prompt = " "
    private val stateCache = mutableMapOf<Long, MutableSet<Long>>()
    private val apiKey = accountProperties.openaiApiKey?.let { "Bearer $it" }
    private var maxTokens = 1000
    private var temperature = 0.1
    private val admin = permissionProperties.admin

    override suspend fun execute(args: List<String>, event: MessageEvent): Message? {
        val (subject, sender, message) = event.getInfo()
        if (permissionProperties.chatExcludeGroup.contains(subject.id)) return null

        if (args.isNotEmpty() && admin.isNotEmpty() && admin.contains(sender.id)) {
            when (args[0]) {
                "temperature" -> {
                    if (args.size < 2) return "参数错误".toPlainText()
                    temperature = args[1].toDouble()
                    return "temperature更改成功".toPlainText()
                }
            }
        }

        val cache = stateCache[sender.id]
        if (cache == null) stateCache[sender.id] = mutableSetOf(subject.id)
        else {
            if (cache.contains(subject.id)) return null
            else stateCache[sender.id]?.add(subject.id)
        }

        val name = sender.nameCardOrNick
        var buffer = StringBuffer(prompt.format(LocalDate.now()))
        var temp = 0
        try {
            coroutineScope {
                if (args.isEmpty()) {
                    subject.sendMessage(name + "你好，我是ChatGPT，现在开始问答，请@我并说出你的问题")
                } else {
                    subject.sendMessage(name + "你好，我是ChatGPT，问答即将开始")
                    buffer.append(args.joinToString("\n"))
                    completion(buffer).let { subject.sendMessage(message.quote() + it) }
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
                    try {
                        temp = buffer.length
                        buffer.append("\n\n${content.trim()}")
                        completion(buffer).let { subject.sendMessage(next.quote() + it) }
                    } catch (e: Exception) {
                        if (buffer.length > 3200) {
                            subject.sendMessage(At(sender) + "上下文长度超过限制，已清除上下文，请重试")
                            buffer = StringBuffer(prompt.format(LocalDate.now()))
                        } else {
                            buffer.setLength(temp)
                            subject.sendMessage(At(sender) + "出现错误，请重试")
                            e.printStackTrace()
                        }
                    }
                }
            }
        } catch (_: TimeoutCancellationException) {
            subject.sendMessage(At(event.sender.id) + "超时，问答已结束")
        } finally {
            stateCache[sender.id]?.remove(subject.id)
        }

        return null
    }

    fun completion(buffer: StringBuffer): String {
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
        return text.split("\n\n").joinToString("\n")
    }

}
