package org.hff.miraiomnitrix.event

import com.google.common.collect.EvictingQueue
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.listener.GroupListener
import java.util.*

object Repeat {
    private val groupMsgMap = GroupListener.groupMsgMap
    private val random = Random()
    private val breaks = arrayOf("break", "打断")
    private val bound = breaks.size

    suspend fun breakRepeat(group: Group, message: MessageChain) {
        var text = message.contentToString()
        if (text.isBlank()) return

        var isImage = false
        val image = message[Image.Key]
        if (image != null) {
            text = image.imageId
            isImage = true
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
            val num = random.nextInt(bound * 2)
            if (num < bound) {
                group.sendMessage(breaks[num])
            } else if (isImage) {
                group.sendMessage(Image(text))
            } else {
                group.sendMessage(text)
            }
        } else {
            stringQueue.add(text)
        }

    }
}
