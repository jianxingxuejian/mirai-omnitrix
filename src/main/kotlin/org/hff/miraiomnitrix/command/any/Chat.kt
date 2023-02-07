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
    private var temperature = 0.1
    private val admin = permissionProperties.admin

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

        if (args.isNotEmpty() && admin.isNotEmpty() && admin.contains(sender.id)) {
            when (args[0]) {
                "update", "select", "修改", "选择" -> {
                    if (args.size < 2) return result("参数错误")
                    model = args[1]
                    subject.sendMessage("模型更改成功")
                }

                "temperature" -> {
                    if (args.size < 2) return result("参数错误")
                    temperature = args[1].toDouble()
                    subject.sendMessage("temperature更改成功")
                }
            }
        }

        val cache = stateCache[sender.id]
        if (cache == null) stateCache[sender.id] = mutableSetOf(subject.id)
        else {
            if (cache.contains(subject.id)) return result("问答线程已经开始，请@我并说出你的问题")
            else stateCache[sender.id]?.add(subject.id)
        }

        val name = sender.nameCardOrNick
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val buffer =
                    StringBuffer("$name: 你是ChatGPT, 一个由OpenAI训练的大型语言模型。现在的时间是:${LocalDate.now()}，开始问答式交流。")
                if (args.isEmpty()) subject.sendMessage(name + "你好，我是ChatGPT，现在开始问答，请@我并说出你的问题")
                else {
                    subject.sendMessage(name + "你好，我是ChatGPT，问答即将开始")
                    buffer.append("${args.joinToString("\n")}\n\n")
                    val reply = completion(buffer, name)
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
                        if (isChat) {
                            if (content.length <= 5) {
                                subject.sendMessage(At(sender) + "请输入")
                                continue
                            } else content.substring(5)
                        } else continue
                    }
                    val temp = buffer.length
                    buffer.append("$name：$content\n\n")
                    try {
                        val reply = completion(buffer, sender.nameCardOrNick)
                        subject.sendMessage(next.quote() + reply)
                    } catch (_: Exception) {
                        buffer.setLength(temp)
                        subject.sendMessage(At(event.sender.id) + "出现错误，请重试")
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