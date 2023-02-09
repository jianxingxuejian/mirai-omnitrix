package org.hff.miraiomnitrix.event.group

import com.google.common.collect.EvictingQueue
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.event.Event
import org.hff.miraiomnitrix.result.EventResult
import org.hff.miraiomnitrix.result.EventResult.Companion.next
import org.hff.miraiomnitrix.utils.SpringUtil
import java.util.*

@Event(priority = 2)
class Repeat : GroupEvent {

    private val permissionProperties = SpringUtil.getBean(PermissionProperties::class)
    private val groupMsgMap = mutableMapOf<Long, Queue<String>>()
    private val random = Random()
    private val breaks = arrayOf("break", "打断")
    private val bound = breaks.size
    override suspend fun handle(
        sender: Member,
        message: MessageChain,
        group: Group,
        args: List<String>,
        event: GroupMessageEvent
    ): EventResult {
        if (args.isEmpty()) return next()
        var content = message.contentToString()
        val excludeGroup = permissionProperties?.repeatExcludeGroup
        if (!excludeGroup.isNullOrEmpty() && excludeGroup.contains(group.id)) return next()

        var isImage = false
        val image = message[Image.Key]
        if (image != null) {
            content = image.imageId
            isImage = true
        }

        if (groupMsgMap[group.id] == null) {
            groupMsgMap[group.id] = EvictingQueue.create(10)
        }

        val stringQueue = groupMsgMap[group.id]!!

        if (stringQueue.count() < 3) {
            stringQueue.add(content)
            return next()
        }

        val last = stringQueue.toList().takeLast(3)
        if (content == last[0] && last[0] == last[1] && last[1] == last[2]) {
            stringQueue.removeIf { it.equals(content) }
            val num = random.nextInt(bound * 2)
            if (num < bound) {
                group.sendMessage(breaks[num])
            } else if (isImage) {
                group.sendMessage(Image(content))
            } else {
                group.sendMessage(message)
            }
        } else {
            stringQueue.add(content)
        }
        return next()
    }


}