package org.hff.miraiomnitrix.command.impl.any

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.app.service.CharacterService
import org.hff.miraiomnitrix.command.core.Command
import org.hff.miraiomnitrix.command.type.AnyCommand
import org.hff.miraiomnitrix.event.Chat.characterCache
import org.hff.miraiomnitrix.event.Chat.characterName
import org.hff.miraiomnitrix.event.Chat.chatting
import org.hff.miraiomnitrix.event.Chat.concatId
import org.hff.miraiomnitrix.event.Chat.token
import org.hff.miraiomnitrix.result.ResultMessage
import org.hff.miraiomnitrix.result.result
import org.hff.miraiomnitrix.utils.HttpUtil.postStringByProxy
import org.hff.miraiomnitrix.utils.JsonUtil
import org.hff.miraiomnitrix.utils.JsonUtil.getAsStr

@Command(name = ["character", "角色"])
class Character(private val characterService: CharacterService) : AnyCommand {

    private val url = "https://beta.character.ai/chat/"

    override suspend fun execute(
        sender: User,
        message: MessageChain,
        subject: Contact,
        args: List<String>
    ): ResultMessage? {
        if (args.isEmpty()) {
            return result(characterService.getCharactersName() + "\n" + "请使用指令+角色名开始聊天" + "\n" + "增删改查格式为 add/del/edit 角色名 角色external_id(add/edit操作)")
        }

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

            "edit", "update" -> {
                if (args.size < 3) return result("参数错误")
                val count = characterService.getCountByName(args[1])
                if (count < 1) {
                    return result("该角色不存在")
                }
                val edit = characterService.edit(args[1], args[2])
                return if (edit) {
                    result("角色编辑成功")
                } else {
                    result("角色编辑失败")
                }
            }
        }

        if (chatting) return null

        val name = args[0]
        if (characterCache[name] != null) return null

        val entity = characterService.get(name) ?: return result(" 角色不存在")

        if (token == null) return result("无token")
        val headers = mapOf("token" to token)
        val param = mapOf("external_id" to entity.externalId)
        val info = postStringByProxy(url + "character/info/", param, headers)
        val history = postStringByProxy(url + "history/create/", param, headers)

        val character = JsonUtil.getObj(info, "character")
        val identifier = character.getAsStr("identifier").replace("id", "internal_id")
        val greeting = character.getAsStr("greeting").replace("{{user}}", sender.nick)
        val historyExternalId = JsonUtil.getStr(history, "external_id")

        characterCache[name] = Triple(historyExternalId, entity.externalId!!, identifier)
        subject.sendMessage(greeting)
        chatting = true
        concatId = subject.id
        characterName = name

        return null
    }

}