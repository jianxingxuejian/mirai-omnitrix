package org.hff.miraiomnitrix.event

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.utils.SpringUtil

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

    /** 非指令消息按照事件优先度进行处理 */
    suspend fun handle(args: List<String>, event: MessageEvent) {
        when (event) {
            is GroupMessageEvent -> groupHandle(args, event)
            is FriendMessageEvent -> friendHandle(args, event)
            else -> {}
        }
        anyHandle(args, event)
    }

    private suspend fun groupHandle(args: List<String>, event: GroupMessageEvent) {
        for (handler in groupChain) {
            val (stop, msg, msgChain) = handler.handle(args, event)
            handleMessage(event.group, msg, msgChain)
            if (stop) break
        }
    }

    private suspend fun friendHandle(args: List<String>, event: FriendMessageEvent) {
        for (handler in friendChain) {
            val (stop, msg, msgChain) = handler.handle(args, event)
            handleMessage(event.friend, msg, msgChain)
            if (stop) break
        }
    }

    private suspend fun anyHandle(args: List<String>, event: MessageEvent) {
        for (handler in anyChain) {
            val (stop, msg, msgChain) = handler.handle(args, event)
            handleMessage(event.subject, msg, msgChain)
            if (stop) break
        }
    }

    private suspend fun handleMessage(subject: Contact, msg: String?, msgChain: MessageChain?) {
        if (msg != null) subject.sendMessage(msg)
        if (msgChain != null) subject.sendMessage(msgChain)
    }
}