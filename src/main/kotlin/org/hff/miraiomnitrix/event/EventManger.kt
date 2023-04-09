package org.hff.miraiomnitrix.event

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.events.UserMessageEvent
import org.hff.miraiomnitrix.common.errorCache
import org.hff.miraiomnitrix.common.sendAndCache
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
        sortByPriority(listOf(anyChain, groupChain, userChain))
    }

    private fun sortByPriority(chains: List<MutableList<out Any>>) =
        chains.forEach { it.sortBy { chain -> chain.javaClass.getAnnotation(Event::class.java).priority } }

    /** 非指令消息按照事件优先度进行处理 */
    suspend fun handle(args: List<String>, event: GroupMessageEvent, isAt: Boolean) {
        groupChain.handleBatch(args, event, isAt)
        anyChain.handleBatch(args, event, isAt)
    }

    suspend fun handle(args: List<String>, event: UserMessageEvent) {
        userChain.handleBatch(args, event)
        anyChain.handleBatch(args, event)
    }

    private suspend inline fun <T : MessageEvent, K : Handler<T>> MutableList<K>.handleBatch(
        args: List<String>,
        event: T,
        isAt: Boolean = false
    ) = event.apply {
        forEach {
            it.run {
                try {
                    val (stop, message) = handle(args, isAt)
                    subject.sendAndCache(message)
                    if (stop) return@apply
                } catch (e: Exception) {
                    e.printStackTrace()
                    errorCache.put(subject.id, e)
                    subject.sendAndCache((e.message ?: "未知错误").toPlainText())
                }
            }
        }
    }

}
