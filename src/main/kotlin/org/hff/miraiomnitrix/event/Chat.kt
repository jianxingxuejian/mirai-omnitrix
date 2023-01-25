package org.hff.miraiomnitrix.event

import net.mamoe.mirai.contact.Contact
import org.hff.miraiomnitrix.config.AccountProperties
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.JsonUtil
import org.hff.miraiomnitrix.utils.SpringUtil

object Chat {
    val token = SpringUtil.getBean(AccountProperties::class)?.characterAiToken
    private const val url = "https://beta.character.ai/chat/streaming/"
    val characterCache = mutableMapOf<String, Triple<String, String, String>>()
    var chatting = false
    var concatId: Long? = null
    var characterName: String? = null

    suspend fun chat(text: String, subject: Contact) {
        if (subject.id != concatId) return

        if (text == "finish" || text == "结束") {
            chatting = false
            concatId = null
            characterCache.remove(characterName)
            characterName = null
            return
        }

        if (token == null) {
            subject.sendMessage("无token")
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
        val headers = mapOf("token" to token)
        val result =
            HttpUtil.postStringByProxy(url, chatParams, headers)
        val replies = JsonUtil.getArray(result, "replies")
            .joinToString("\n") { JsonUtil.getStr(it, "text") }
        subject.sendMessage(replies)
    }
}
