package org.hff.miraiomnitrix.command.any

import com.aallam.openai.api.completion.CompletionRequest
import com.aallam.openai.api.exception.OpenAIHttpException
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.model.Model
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.google.common.cache.CacheBuilder
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
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
import org.hff.miraiomnitrix.result.CommandResult
import org.hff.miraiomnitrix.result.CommandResult.Companion.result
import java.util.concurrent.TimeUnit

@Command(["chat", "聊天"])
class Chat(accountProperties: AccountProperties, permissionProperties: PermissionProperties) : AnyCommand {

    private lateinit var openAI: OpenAI
    private var model: Model? = null
    private val chatIncludeGroup = permissionProperties.chatIncludeGroup
    private val initModel = "text-davinci-003"
    private val historyCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build<Long, Long>()

    init {
        val apiKey = accountProperties.openaiApiKey
        if (apiKey != null) {
            openAI = OpenAI(OpenAIConfig(apiKey, logLevel = LogLevel.Body))
        }
    }

    override suspend fun execute(
        sender: User,
        message: MessageChain,
        subject: Contact,
        args: List<String>,
        event: MessageEvent
    ): CommandResult? {
        if (args.isEmpty()) {
            return result("使用openai进行聊天，通过`开始`、`start`指令开启聊天，需要@机器人，群成员与机器人的交流是独立的")
        }
        if (subject is Group && chatIncludeGroup.isNotEmpty()) {
            if (!chatIncludeGroup.contains(subject.id)) return null
        }

        when (args[0]) {
            "开始聊天", "start" -> {
                val cache = historyCache.getIfPresent(subject.id)
                if (cache == null) historyCache.put(subject.id, sender.id)
                else if (cache == sender.id) return result(At(sender) + "问答进程已启动，请@机器人进行问答")
                subject.sendMessage("${sender.nameCardOrNick}你好，我是openai的${model?.id?.id ?: initModel}模型，现在开始问答，请@我并输入你要问的问题")
                if (args.size < 2) chat(event, null)
                else chat(event, args.slice(1 until args.size).joinToString("\n"))
                return null
            }

            "所有模型", "models" -> {
                val models = openAI.models()
                println(models)
                val list = models.mapIndexed { index, model -> "${index + 1}、${model.id.id}" }.joinToString("\n")
                val text = if (model?.id != null) "当前使用的模型为${model!!.id}" else "当前没有模型"
                return result(list + "\n$text")
            }

            "选择模型", "select" -> {
                if (args.size < 2) return result("参数错误")
                model = openAI.model(ModelId(args[1]))
                return result("模型更改成功")
            }
        }

        return null

    }

    private suspend fun chat(event: MessageEvent, text: String?) {
        try {
            val sender = event.sender.nameCardOrNick
            val buffer = StringBuffer("$sender：你是ChatGPT，一个由openAI训练的大型语言模型，现在开始交流。\n")
            if (text != null) {
                buffer.append("$text\n")
                val reply = completion(buffer, sender)
                event.subject.sendMessage(event.message.quote() + reply.removePrefix("\n\n"))
            }
            coroutineScope {
                while (isActive) {
                    val next = event.nextMessage(300_000L, EventPriority.HIGH, intercept = true)
                    val content = next.contentToString()
                    val at = "@" + event.bot.id
                    if (!content.startsWith(at)) continue
                    buffer.append("$sender：${content.replace(at, "")}\n")
                    val reply = completion(buffer, sender)
                    event.subject.sendMessage(next.quote() + reply.removePrefix("\n\n"))
                }
            }
        } catch (_: TimeoutCancellationException) {
        } catch (_: OpenAIHttpException) {
            event.subject.sendMessage(At(event.sender.id) + "网络错误，问答已结束")
        } finally {
            historyCache.invalidate(event.subject.id)
        }
    }

    private suspend fun completion(buffer: StringBuffer, user: String): String {
        if (model == null) {
            model = openAI.model(ModelId(initModel))
        }
        val completionRequest = CompletionRequest(
            model = model!!.id,
            user = user,
            prompt = buffer.toString(),
            maxTokens = 1024
        )
        val choices = openAI.completion(completionRequest).choices
        val replay = choices.joinToString("\n") { it.text.split("\n\n").joinToString("\n") }
        buffer.append("$replay\n")
        return replay
    }

}