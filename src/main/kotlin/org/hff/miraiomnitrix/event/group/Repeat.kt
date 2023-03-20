package org.hff.miraiomnitrix.event.group

import com.google.common.collect.EvictingQueue
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.event.*
import org.hff.miraiomnitrix.utils.getInfo
import java.util.*

@Event(priority = 4)
class Repeat(private val permissionProperties: PermissionProperties) : GroupEvent {

    private val groupMsgMap = hashMapOf<Long, Queue<String>>()
    private val random = Random()
    private val breaks = arrayOf("break", "打断")
    override suspend fun handle(args: List<String>, event: GroupMessageEvent, isAt: Boolean): EventResult {
        if (args.isEmpty()) return next()
        val (group, _, message) = event.getInfo()
        if (permissionProperties.repeatExcludeGroup.contains(group.id)) return next()

        val (content, isImage) = message[Image.Key]?.run { imageId to true } ?: (message.content to false)

        val stringQueue = groupMsgMap.getOrPut(group.id) { EvictingQueue.create(10) }

        if (stringQueue.count() < 3) stringQueue.apply { add(content) }.run { return next() }

        val last = stringQueue.toList().takeLast(3)
        return if (content == last[0] && last[0] == last[1] && last[1] == last[2]) {
            stringQueue.removeIf { it == content }
            when (val num = random.nextInt(breaks.size * 2)) {
                in breaks.indices -> breaks[num].toPlainText()
                else -> if (isImage) Image(content) else message
            }.let(::stop)
        } else {
            stringQueue.add(content)
            next()
        }
    }

}
