package org.hff.miraiomnitrix.event.any

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.result.EventResult

interface AnyEvent {

    suspend fun handle(
        sender: User,
        message: MessageChain,
        subject: Contact,
        args: List<String>,
        event: MessageEvent
    ): EventResult

}