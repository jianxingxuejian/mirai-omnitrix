package org.hff.miraiomnitrix.command.any

import cn.hutool.json.JSONUtil
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.app.service.CharacterService
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.listener.AnyListener.characterCache
import org.hff.miraiomnitrix.listener.AnyListener.characterName
import org.hff.miraiomnitrix.listener.AnyListener.chatting
import org.hff.miraiomnitrix.listener.AnyListener.concatId
import org.hff.miraiomnitrix.listener.AnyListener.token
import org.hff.miraiomnitrix.result.ResultMessage
import org.hff.miraiomnitrix.result.fail
import org.hff.miraiomnitrix.result.result
import org.hff.miraiomnitrix.utils.HttpUtil.postStringByProxy

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
        val param = mapOf("external_id" to entity.externalId)
        val response1 = postStringByProxy(url + "character/info/", param, token)
        if (response1?.statusCode() != 200) return fail()
        val response2 = postStringByProxy(url + "history/create/", param, token)
        if (response2?.statusCode() != 200) return fail()

        val character = JSONUtil.parseObj(response1.body()).getJSONObject("character")
        val identifier = character.getStr("identifier").replace("id", "internal_id")
        val greeting = character.getStr("greeting").replace("{{user}}", sender.nick)
        val historyExternalId = JSONUtil.parseObj(response2.body()).getStr("external_id")

        characterCache[name] = Triple(historyExternalId, entity.externalId!!, identifier)
        subject.sendMessage(greeting)
        chatting = true
        concatId = subject.id
        characterName = name

        return null
    }

}