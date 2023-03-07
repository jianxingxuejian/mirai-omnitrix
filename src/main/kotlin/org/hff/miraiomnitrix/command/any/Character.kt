package org.hff.miraiomnitrix.command.any

import com.google.common.cache.CacheBuilder
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.nextMessage
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.CommandResult
import org.hff.miraiomnitrix.command.result
import org.hff.miraiomnitrix.config.AccountProperties
import org.hff.miraiomnitrix.db.service.CharacterService
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.JsonUtil
import org.hff.miraiomnitrix.utils.JsonUtil.getAsStr
import org.hff.miraiomnitrix.utils.getInfo
import java.util.concurrent.TimeUnit

@Command(name = ["character", "角色"])
class Character(
    private val characterService: CharacterService,
    private val accountProperties: AccountProperties
) : AnyCommand {

    private val url = "https://beta.character.ai/chat"
    private val historyCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build<Long, Long>()

    fun help() = result(
        """可用角色如下: ${characterService.getCharactersName()}
            |请使用指令+角色名开始聊天
            |新增角色 add 角色名 角色external_id
            |删除角色 del 角色名
            """.trimMargin()
    )

    override suspend fun execute(args: List<String>, event: MessageEvent): CommandResult? {
        if (args.isEmpty()) return help()

        when (args[0]) {
            "add" -> {
                if (args.size < 3) return result("参数错误")
                val count = characterService.getCountByName(args[1])
                if (count > 0) {
                    return result("该角色已存在")
                }
                val add = characterService.add(args[1], args[2])
                return if (add) {
                    result("角色添加成功")
                } else {
                    result("角色添加失败")
                }
            }

            "del", "remove" -> {
                if (args.size < 2) return result("参数错误")
                val del = characterService.del(args[1])
                return if (del) {
                    result("角色删除成功")
                } else {
                    result("角色删除失败")
                }
            }

        }

        val (subject, sender) = event.getInfo()

        val cache = historyCache.getIfPresent(subject.id)
        if (cache == null) historyCache.put(subject.id, sender.id)
        else if (cache == sender.id) return result(At(sender) + "角色进程已启动，请@机器人进行问答")

        val entity = characterService.get(args[0]) ?: return result(" 角色不存在")
        val token = accountProperties.characterAiToken ?: return result("无token")
        val headers = mapOf("authorization" to token,"User-Agent" to "PostmanRuntime/7.31.1")
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
                        .joinToString("\n") { JsonUtil.getStr(it, "text") }
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