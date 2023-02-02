package org.hff.miraiomnitrix.handler.type

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.MessageChain

interface AnyHandler {

    suspend fun handle(message: MessageChain, contact: Contact): Boolean

}