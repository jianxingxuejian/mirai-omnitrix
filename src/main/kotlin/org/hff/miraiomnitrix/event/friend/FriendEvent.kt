package org.hff.miraiomnitrix.event.friend

import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.result.EventResult

interface FriendEvent {

    suspend fun handle(
        sender: Friend,
        message: MessageChain,
        args: List<String>,
        event: FriendMessageEvent
    ): EventResult

}