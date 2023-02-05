package org.hff.miraiomnitrix.event.type

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.result.EventResult

/** 群消息处理 */
interface GroupEvent {

    suspend fun handle(
        sender: Member,
        message: MessageChain,
        group: Group,
        args: List<String>,
        event: GroupMessageEvent
    ): EventResult

}