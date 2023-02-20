package org.hff.miraiomnitrix.event.group

import com.google.common.collect.EvictingQueue
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.event.Event
import org.hff.miraiomnitrix.event.EventResult
import org.hff.miraiomnitrix.event.GroupEvent
import org.hff.miraiomnitrix.event.next
import org.hff.miraiomnitrix.utils.getInfo
import java.util.*

@Event(priority = 2)
class Repeat(private val permissionProperties: PermissionProperties) : GroupEvent {

    private val groupMsgMap = mutableMapOf<Long, Queue<String>>()
    private val random = Random()
    private val breaks = arrayOf("break", "打断")
    private val bound = breaks.size
    override suspend fun handle(args: List<String>, event: GroupMessageEvent): EventResult {
        if (args.isEmpty()) return next()
        val (group, _, message) = event.getInfo()
        var content = message.contentToString()
        if (permissionProperties.repeatExcludeGroup.contains(group.id)) return next()

        var isImage = false
        val image = message[Image.Key]
        if (image != null) {
            content = image.imageId
            isImage = true
        }

        if (groupMsgMap[group.id] == null) groupMsgMap[group.id] = EvictingQueue.create(10)

        val stringQueue = groupMsgMap[group.id]!!

        if (stringQueue.count() < 3) {
            stringQueue.add(content)
            return next()
        }

        val last = stringQueue.toList().takeLast(3)
        if (content == last[0] && last[0] == last[1] && last[1] == last[2]) {
            stringQueue.removeIf { it.equals(content) }
            val num = random.nextInt(bound * 2)
            if (num < bound) group.sendMessage(breaks[num])
            else if (isImage) group.sendMessage(Image(content))
            else group.sendMessage(message)
        } else {
            stringQueue.add(content)
        }
        return next()
    }


}