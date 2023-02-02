package org.hff.miraiomnitrix.event.type

import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.message.data.MessageChain

interface FriendHandler {

    suspend fun handle(sender: Friend, message: MessageChain, args: List<String>): Boolean

}