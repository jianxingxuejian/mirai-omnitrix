package org.hff.miraiomnitrix.event.group

import com.google.common.util.concurrent.RateLimiter
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image
import org.hff.miraiomnitrix.event.Event
import org.hff.miraiomnitrix.event.EventResult
import org.hff.miraiomnitrix.event.GroupEvent
import org.hff.miraiomnitrix.event.next
import org.hff.miraiomnitrix.utils.Util.startsWithMany
import java.time.LocalTime.*

@Event(priority = 1)
class Reply : GroupEvent {

    private val limiter = RateLimiter.create(5.0 / 60.0)

    override suspend fun handle(args: List<String>, event: GroupMessageEvent): EventResult {
        if (args.isEmpty()) return next()
        val group = event.group
        val arg = args[0]
        with(limiter) {
            if (tryAcquire()) {
                when (arg) {
                    "day0" -> group.sendMessage("day0")
                    "贴贴" -> group.sendMessage("贴贴")
                    "嘻嘻" -> group.sendMessage("嘻嘻")
                    "笨蛋" -> group.sendMessage("八嘎")
                    "8" -> group.sendMessage("10")
                    "炼" -> group.sendMessage("不许炼")
                }
            }
        }
        when {
            arg.startsWithMany("睡觉", "晚安") -> {
                if (now() in MIDNIGHT..of(6, 0)) {
                    group.sendMessage(Image("{1DB34403-D6DA-5C2C-C3D2-EF86C4D5E7CF}.gif"))
                } else {
                    group.sendMessage("你这个年龄段你睡得着觉?")
                }
            }

            args[0].startsWithMany("泪目", "哭了") -> group.sendMessage("擦擦")
        }
        return next()
    }

}