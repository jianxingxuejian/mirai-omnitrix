package org.hff.miraiomnitrix.command.impl.any

import com.aallam.openai.api.completion.CompletionRequest
import com.aallam.openai.api.model.Model
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.command.core.Command
import org.hff.miraiomnitrix.command.type.AnyCommand
import org.hff.miraiomnitrix.config.AccountProperties
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.result.ResultMessage
import org.hff.miraiomnitrix.result.result

@Command(["chat", "聊天"])
class Chat(accountProperties: AccountProperties, permissionProperties: PermissionProperties) : AnyCommand {

    private lateinit var openAI: OpenAI
    private var model: Model? = null
    private val chatIncludeGroup = permissionProperties.chatIncludeGroup

    init {
        val apiKey = accountProperties.openaiApiKey
        if (apiKey != null) {
            openAI = OpenAI(apiKey)
        }
    }

    override suspend fun execute(
        sender: User,
        message: MessageChain,
        subject: Contact,
        args: List<String>
    ): ResultMessage? {
        if (args.isEmpty()) {
            return result("使用openai开始聊天，通过回复机器人来衔接上下文")
        }
        if (chatIncludeGroup.isNotEmpty()) {
            if (!chatIncludeGroup.contains(subject.id)) return null
        }

        if (model == null) {
            model = openAI.model(ModelId("text-davinci-003"))
        }

        val text = args.joinToString("")

        val completionRequest = CompletionRequest(
            model = model!!.id,
            user = sender.nameCardOrNick,
            prompt = text,
            maxTokens = 1024
        )
        val choices = openAI.completion(completionRequest).choices
        choices.joinToString("\n") { it.text.replace("\\n", "") }
        return result(choices.joinToString("\n") { it.text.replace("\\n", "") })

    }

}