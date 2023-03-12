package org.hff.miraiomnitrix.event.group

import com.google.common.util.concurrent.RateLimiter
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.PlainText
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.event.Event
import org.hff.miraiomnitrix.event.EventResult
import org.hff.miraiomnitrix.event.GroupEvent
import org.hff.miraiomnitrix.event.next
import org.hff.miraiomnitrix.utils.getInfo
import java.time.LocalTime.*

@Event(priority = 2)
class AutoReply(permissionProperties: PermissionProperties) : GroupEvent {

    val limiterMap = permissionProperties.replyIncludeGroup.associateWith { RateLimiter.create(0.1) }

    private val constMap = mapOf(
        "day0" to "day0",
        "贴贴" to "贴贴",
        "嘻嘻" to "嘻嘻",
        "笨蛋" to "八嘎",
        "8" to "10",
        "吃饭了" to "好饿",
        "?" to "¿",
        "？" to "¿",
        "??" to "???",
        "？？" to "？？？",
        "好活" to "活好",
        "小黑子" to "你干嘛～～哎呦",
        "真的" to "假的",
        "假的" to "真的",
    )
    private val replyMap = mapOf(
        "炼" to "不许炼",
        "好累啊" to "累就对了",
        "涩涩" to "不可以涩涩",
        "不可以涩涩" to "不涩涩就挨打",
        "不准涩涩" to "不涩涩就挨打",
    )
    private val regexMap = mapOf<Regex, () -> Message>(
        Regex("^(睡觉|晚安).*") to {
            if (now() in MIDNIGHT..of(6, 0)) Image("{1DB34403-D6DA-5C2C-C3D2-EF86C4D5E7CF}.gif")
            else PlainText("你这个年龄段你睡得着觉?")
        },
        Regex("^(泪目|哭了).*") to { PlainText("擦擦") },
        Regex("^笑死.*") to { Image("{65929E6A-55E8-AF62-D73B-DA8F7F49EA81}.jpg") },
    )

    override suspend fun handle(args: List<String>, event: GroupMessageEvent, isAt: Boolean): EventResult {
        val limiter = limiterMap[event.group.id] ?: return next()
        if (args.isEmpty()) return next()

        val (group, _, message) = event.getInfo()
        val arg = args[0]

        with(limiter) {
            when (arg) {
                in constMap.keys ->
                    constMap[arg]?.takeIf { tryAcquire() }?.let { group.sendMessage(it) }

                in replyMap.keys ->
                    replyMap[arg]?.takeIf { tryAcquire() }?.let { group.sendMessage(message.quote() + it) }

                else -> regexMap.entries.find { it.key.matches(arg) }?.value?.takeIf { tryAcquire() }
                    ?.let { group.sendMessage(it.invoke()) }
            }
        }

        return next()
    }
}