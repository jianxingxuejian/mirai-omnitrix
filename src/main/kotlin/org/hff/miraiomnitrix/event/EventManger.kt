package org.hff.miraiomnitrix.event

import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.event.any.BiliBili
import org.hff.miraiomnitrix.event.any.Cache
import org.hff.miraiomnitrix.event.any.Img
import org.hff.miraiomnitrix.event.any.Search
import org.hff.miraiomnitrix.event.friend.FriendEvent
import org.hff.miraiomnitrix.event.group.Pa
import org.hff.miraiomnitrix.event.group.Repeat

object EventManger {

    private val anyChain = listOf(BiliBili, Cache, Img, Search)
    private val groupChain = listOf(Pa, Repeat)
    private val friendChain = listOf<FriendEvent>()

    suspend fun groupHandle(
        sender: Member,
        message: MessageChain,
        group: Group,
        args: List<String>,
        event: GroupMessageEvent
    ) {
        for (handler in groupChain) {
            val (stop, msg, msgChain) = handler.handle(sender, message, group, args, event)
            handleMessage(group, msg, msgChain)
            if (stop) break
        }
        anyHandle(sender, message, group, args, event)
    }

    suspend fun friendHandle(friend: Friend, message: MessageChain, args: List<String>, event: FriendMessageEvent) {
        for (handler in friendChain) {
            val (stop, msg, msgChain) = handler.handle(friend, message, args, event)
            handleMessage(friend, msg, msgChain)
            if (stop) break
        }
        anyHandle(friend, message, friend, args, event)
    }

    private suspend fun anyHandle(
        sender: User,
        message: MessageChain,
        subject: Contact,
        args: List<String>,
        event: MessageEvent
    ) {
        for (handler in anyChain) {
            val (stop, msg, msgChain) = handler.handle(sender, message, subject, args, event)
            handleMessage(subject, msg, msgChain)
            if (stop) break
        }
    }

    private suspend fun handleMessage(subject: Contact, msg: String?, msgChain: MessageChain?) {
        if (msg != null) subject.sendMessage(msg)
        if (msgChain != null) subject.sendMessage(msgChain)
    }
}