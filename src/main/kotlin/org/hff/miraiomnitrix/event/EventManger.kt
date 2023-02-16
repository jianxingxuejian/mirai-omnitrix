package org.hff.miraiomnitrix.event

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.event.any.AnyEvent
import org.hff.miraiomnitrix.event.friend.FriendEvent
import org.hff.miraiomnitrix.event.group.GroupEvent
import org.hff.miraiomnitrix.utils.SpringUtil
import org.hff.miraiomnitrix.utils.Util.getInfo

object EventManger {

    private val anyChain = mutableListOf<AnyEvent>()
    private val groupChain = mutableListOf<GroupEvent>()
    private val friendChain = mutableListOf<FriendEvent>()

    init {
        SpringUtil.getBeansWithAnnotation(Event::class)?.values?.forEach {
            when (it) {
                is AnyEvent -> anyChain.add(it)
                is GroupEvent -> groupChain.add(it)
                is FriendEvent -> friendChain.add(it)
            }
        }
        sortByPriority(anyChain)
        sortByPriority(groupChain)
        sortByPriority(friendChain)
    }

    private fun sortByPriority(chain: MutableList<out Any>) =
        chain.sortBy { it.javaClass.getAnnotation(Event::class.java).priority }

    suspend fun groupHandle(args: List<String>, event: GroupMessageEvent) {
        val (sender, message, group) = getInfo(event)
        for (handler in groupChain) {
            val (stop, msg, msgChain) = handler.handle(sender, message, group, args, event)
            handleMessage(group, msg, msgChain)
            if (stop) break
        }
        anyHandle(sender, message, group, args, event)
    }

    suspend fun friendHandle(args: List<String>, event: FriendMessageEvent) {
        val (friend, message) = getInfo(event)
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