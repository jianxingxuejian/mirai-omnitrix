package org.hff.miraiomnitrix.event

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.events.UserMessageEvent
import org.hff.miraiomnitrix.utils.SpringUtil

object EventManger {

    private val anyChain = mutableListOf<AnyEvent>()
    private val groupChain = mutableListOf<GroupEvent>()
    private val userChain = mutableListOf<UserEvent>()

    init {
        SpringUtil.getBeansWithAnnotation(Event::class)?.values?.forEach {
            when (it) {
                is AnyEvent -> anyChain.add(it)
                is GroupEvent -> groupChain.add(it)
                is UserEvent -> userChain.add(it)
            }
        }
        sortByPriority(anyChain)
        sortByPriority(groupChain)
        sortByPriority(userChain)
    }

    private fun sortByPriority(chain: MutableList<out Any>) =
        chain.sortBy { it.javaClass.getAnnotation(Event::class.java).priority }

    /** 非指令消息按照事件优先度进行处理 */
    suspend fun handle(args: List<String>, event: MessageEvent, isAt: Boolean) {
        when (event) {
            is GroupMessageEvent -> {
                groupChain.handle(args, event, isAt)
                anyChain.handle(args, event)
            }

            is UserMessageEvent -> {
                userChain.handle(args, event)
                anyChain.handle(args, event)
            }

            else -> {}
        }
    }

    private suspend fun <T : MessageEvent, K : Handle<T>> MutableList<K>.handle(
        args: List<String>,
        event: T,
        isAt: Boolean = false
    ) {
        for (handler in this) {
            val (stop, msg, msgChain) = handler.handle(args, event, isAt)
            val subject = event.subject
            if (msg != null) subject.sendMessage(msg)
            if (msgChain != null) subject.sendMessage(msgChain)
            if (stop) break
        }
    }

}
