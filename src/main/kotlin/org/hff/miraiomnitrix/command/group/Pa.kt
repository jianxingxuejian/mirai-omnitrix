package org.hff.miraiomnitrix.command.group

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChainBuilder
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.utils.HttpUtil

@Command(name = ["爬"], isNeedHeader = false)
class Pa : GroupCommand {

    private val avatarUrl = "https://q1.qlogo.cn/g?b=qq&s=640&nk=";

    override suspend fun execute(
        sender: Member,
        message: MessageChain,
        group: Group,
        args: List<String>
    ): MessageChain? {
        val qq: Long = args.find { it.startsWith("@") }?.substring(1)?.toLong() ?: return null
        val response = HttpUtil.getInputStream(avatarUrl + qq) ?: return null
        if (response.statusCode() != 200) return null
        val builder: MessageChainBuilder
        return null
    }

}