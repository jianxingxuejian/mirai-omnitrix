package org.hff.miraiomnitrix.event.type

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.MessageChain

/** 群消息处理 */
interface GroupHandler {

    suspend fun handle(sender: Member, message: MessageChain, group: Group, args: List<String>): Boolean

}