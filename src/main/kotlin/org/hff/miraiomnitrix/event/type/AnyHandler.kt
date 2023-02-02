package org.hff.miraiomnitrix.event.type

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.MessageChain

interface AnyHandler {

    suspend fun handle(sender: User, message: MessageChain, subject: Contact, args: List<String>): Boolean

}