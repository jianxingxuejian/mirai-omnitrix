package org.hff.miraiomnitrix.event.core

import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.event.impl.any.BiliBili
import org.hff.miraiomnitrix.event.impl.any.Img
import org.hff.miraiomnitrix.event.impl.group.Pa
import org.hff.miraiomnitrix.event.impl.group.Repect
import org.hff.miraiomnitrix.event.type.FriendHandler

object HandlerManger {

    private val anyChain = listOf(BiliBili, Img)
    private val groupChain = listOf(Pa, Repect)
    private val friendChain = listOf<FriendHandler>()

    suspend fun groupHandle(sender: Member, message: MessageChain, group: Group, args: List<String>) {
        for (handler in groupChain) {
            val exit = handler.handle(sender, message, group, args)
            if (exit) break
        }
        anyHandle(sender, message, group, args)
    }

    suspend fun friendHandle(friend: Friend, message: MessageChain, args: List<String>) {
        for (handler in friendChain) {
            val exit = handler.handle(friend, message, args)
            if (exit) break
        }
        anyHandle(friend, message, friend, args)
    }

    private suspend fun anyHandle(sender: User, message: MessageChain, subject: Contact, args: List<String>) {
        for (handler in anyChain) {
            val exit = handler.handle(sender, message, subject, args)
            if (exit) break
        }
    }
}