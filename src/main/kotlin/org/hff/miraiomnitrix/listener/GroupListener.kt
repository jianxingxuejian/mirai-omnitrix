package org.hff.miraiomnitrix.listener

import com.google.common.collect.EvictingQueue
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.ListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.command.CommandManager
import java.util.*

object GroupListener : ListenerHost {

    private val groupMsgMap = mutableMapOf<Long, Queue<String>>()
    private val random = Random()
    private val breaks = arrayOf("break", "打断")
    private val bound = breaks.size

    @EventHandler
    suspend fun GroupMessageEvent.onMessage() {
        breakRepeat(group, message)

        val (msg, message) = CommandManager.executeGroupCommand(sender, message, group) ?: return
        if (msg != null) {
            group.sendMessage(msg)
        }
        if (message != null) {
            group.sendMessage(message)
        }
    }

    private suspend fun breakRepeat(group: Group, message: MessageChain) {
        var text = message.contentToString()
        if (text.isBlank()) return

        val image = message[Image.Key]
        if (image != null) {
            text = image.imageId
        }

        if (groupMsgMap[group.id] == null) {
            groupMsgMap[group.id] = EvictingQueue.create(10)
        }

        val stringQueue = groupMsgMap[group.id]!!

        if (stringQueue.count() < 3) {
            stringQueue.add(text)
            return
        }

        val last = stringQueue.toList().takeLast(3)
        if (text == last[0] && last[0] == last[1] && last[1] == last[2]) {
            stringQueue.removeIf { it.equals(text) }
            group.sendMessage(getBreakText())
        } else {
            stringQueue.add(text)
        }

    }

    private fun getBreakText() = breaks[random.nextInt(bound)]

}