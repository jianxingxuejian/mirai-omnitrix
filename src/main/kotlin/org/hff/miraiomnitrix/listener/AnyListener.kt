package org.hff.miraiomnitrix.listener

import cn.hutool.json.JSONUtil
import com.google.common.cache.CacheBuilder
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.ListenerHost
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.*
import org.hff.miraiomnitrix.command.CommandManager
import org.hff.miraiomnitrix.utils.HttpUtil.postStringByProxy
import java.util.concurrent.TimeUnit

object AnyListener : ListenerHost {

//    override fun handleException(context: CoroutineContext, exception: Throwable) {
//        println(exception.message)
//    }

    val imageCache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).build<Int, String>()
    val characterCache = mutableMapOf<String, Triple<String, String, String>>()

    var chatting = false
    var concatId: Long? = null
    var characterName: String? = null
    const val token = "Token 43e1a5d057f1225ca2895f28f45a9b4385028fd8"

    @EventHandler
    suspend fun MessageEvent.onMessage() {
        val first = message[1]
        if (first is PlainText && subject.id == concatId) chat(first.content, subject)

        val image = message[Image.Key]
        if (image != null) {
            imageCache.put(message.source.internalIds[0], image.imageId)
        }

        val (msg, message) = CommandManager.executeAnyCommand(sender, message, subject) ?: return
        if (msg != null) {
            subject.sendMessage(msg)
        }
        if (message != null) {
            subject.sendMessage(message)
        }
    }

    private suspend fun chat(text: String, subject: Contact) {
        if (text == "finish" || text == "结束") {
            chatting = false
            concatId = null
            characterCache.remove(characterName)
            characterName = null
            return
        }

        if (!chatting) return

        val (historyExternalId, characterExternalId, identifier) = characterCache[characterName] ?: return
        val chatParams = mapOf(
            "history_external_id" to historyExternalId,
            "character_external_id" to characterExternalId,
            "text" to text,
            "tgt" to identifier
        )
        val response = postStringByProxy("https://beta.character.ai/chat/streaming/", chatParams, token)
        if (response?.statusCode() != 200) {
            subject.sendMessage("网络错误")
            return
        }
        val replies = JSONUtil.parseObj(response.body()).getJSONArray("replies")
            .joinToString("\n") { JSONUtil.parseObj(it).getStr("text") }
        subject.sendMessage(replies)
    }
}