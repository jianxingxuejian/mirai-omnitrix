package org.hff.miraiomnitrix.command.any

import com.google.common.cache.CacheBuilder
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.message.nextMessage
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.config.AccountProperties
import org.hff.miraiomnitrix.db.service.CharacterService
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.JsonUtil
import org.hff.miraiomnitrix.utils.getAsStr
import org.hff.miraiomnitrix.utils.getInfo
import java.util.concurrent.TimeUnit

@Command(name = ["character", "角色"])
class Character(
    private val characterService: CharacterService,
    private val accountProperties: AccountProperties
) : AnyCommand {

    private val url = "https://beta.character.ai/chat"
    private val historyCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build<Long, Long>()

    fun help() = """
        |可用角色如下: ${characterService.getCharactersName()}
        |请使用指令+角色名开始聊天
        |新增角色 add 角色名 角色external_id
        |删除角色 del 角色名
    """.trimMargin().toPlainText()

    override suspend fun execute(args: List<String>, event: MessageEvent): Message? {
        if (args.isEmpty()) return help()

        when (args[0]) {
            "add" -> {
                if (args.size < 3) return "参数错误".toPlainText()
                val count = characterService.getCountByName(args[1])
                if (count > 0) {
                    return "该角色已存在".toPlainText()
                }
                val add = characterService.add(args[1], args[2])
                return (if (add) "角色删除成功" else "角色删除失败").toPlainText()
            }

            "del", "remove" -> {
                if (args.size < 2) return "参数错误".toPlainText()
                val del = characterService.del(args[1])
                return (if (del) "角色删除成功" else "角色删除失败").toPlainText()
            }

        }

        val (subject, sender) = event.getInfo()

        val cache = historyCache.getIfPresent(subject.id)
        if (cache == null) historyCache.put(subject.id, sender.id)
        else if (cache == sender.id) return At(sender) + "角色进程已启动，请@机器人进行问答"

        val entity = characterService.get(args[0]) ?: return "角色不存在".toPlainText()
        val token = accountProperties.characterAiToken ?: return "无token".toPlainText()
        val headers = mapOf(
            "authorization" to token,
            "User-Agent" to "PostmanRuntime/7.31.1",
            "Cookie" to "csrftoken=uSJnatxvL2PxDokScFcKseg0KtiYop37; sessionid=8cmt1y5x1p98bg9azbxzav3bqcysqqy6; __cf_bm=7NUZiwd7R.7wvu5BCavR1FDoOxzp_ZnFQGaIFNIzHsw-1678241526-0-ASJ84dvAB/7Su6wKVrFVKE4IUT7TPomu2mYazBdzfyN/a7P42KrhVTr0ZIY0F2kIFV+VpQcP2EKoaShxsBK/GsZXiXu04UZOxP3Y/+VzILxZpBN9Nf8y5ge+cNu4cBQhwr84avMm6dJFQ2nE2ujY1ZGy0Oci0+u1M63zY/FZkFP7; _ga=GA1.2.1098620751.1678241529; _gid=GA1.2.20046999.1678241529"
        )
        val param = mapOf("character_external_id" to entity.externalId!!)
        chat(entity.externalId!!, event, headers, param)
        return null
    }

    suspend fun chat(
        characterExternalId: String,
        event: MessageEvent,
        headers: Map<String, String>,
        param: Map<String, String>
    ) {
        try {
            val info = HttpUtil.getStringByProxy("$url/character/info-cached/$characterExternalId/", headers)
            val history = HttpUtil.postStringByProxy("$url/history/create/", param, headers)
            val character = JsonUtil.getObj(info, "character")
            val identifier = character.getAsStr("identifier").replace("id", "internal_id")
            val greeting = character.getAsStr("greeting").replace("{{user}}", event.sender.nick)
            val historyExternalId = JsonUtil.getStr(history, "external_id")
            event.subject.sendMessage(At(event.sender.id) + greeting)

            coroutineScope {
                while (isActive) {
                    val next = event.nextMessage(300_000L, EventPriority.HIGH, intercept = true)
                    val content = next.contentToString()
                    val at = "@" + event.bot.id
                    if (!content.startsWith(at)) continue
                    val chatParams = mapOf(
                        "history_external_id" to historyExternalId,
                        "character_external_id" to characterExternalId,
                        "text" to content,
                        "tgt" to identifier
                    )
                    val result = HttpUtil.postStringByProxy(url + "streaming/", chatParams, headers)
                    val replies = JsonUtil.getArray(result, "replies")
                        .joinToString("\n") { it.getAsStr("text") }
                    event.subject.sendMessage(replies)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            event.subject.sendMessage(At(event.sender.id) + "网络错误，聊天已结束")
        } finally {
            historyCache.invalidate(event.subject.id)
        }
    }

}
