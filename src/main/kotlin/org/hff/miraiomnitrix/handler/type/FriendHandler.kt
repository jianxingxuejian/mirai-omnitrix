package org.hff.miraiomnitrix.handler.type

import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.message.data.MessageChain

interface FriendHandler {

    suspend fun handle(message: MessageChain, friend: Friend): Boolean

}