package org.hff.miraiomnitrix.handler.core

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.handler.impl.Repect
import org.hff.miraiomnitrix.handler.type.AnyHandler
import org.hff.miraiomnitrix.handler.type.FriendHandler
import org.hff.miraiomnitrix.handler.type.GroupHandler

object HandlerManger {

    private val anyChain = mutableListOf<AnyHandler>()
    private val groupChain = mutableListOf<GroupHandler>()
    private val friendChain = mutableListOf<FriendHandler>()

    init {
        groupChain.add(Repect)
    }

    fun anyHandle(message: MessageChain, contact: Contact) {}

    suspend fun groupHandle(message: MessageChain, group: Group) {
        for (handler in groupChain) {
            val exit = handler.handle(message, group)
            if (exit) break
        }
    }

    fun friendHandle(message: MessageChain, friend: Friend) {}
}