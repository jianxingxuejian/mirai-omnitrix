package org.hff.miraiomnitrix.event.group

import com.google.common.util.concurrent.RateLimiter
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.PlainText
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.event.*
import org.hff.miraiomnitrix.utils.getInfo
import java.time.LocalTime.*

@Event(priority = 2)
class AutoReply(permissionProperties: PermissionProperties) : GroupEvent {

    val limiterMap = permissionProperties.replyIncludeGroup.associateWith { RateLimiter.create(5.0 / 60.0) }

    private val constMap = mapOf(
        "day0" to "day0",
        "贴贴" to "贴贴",
        "嘻嘻" to "嘻嘻",
        "笨蛋" to "八嘎",
        "8" to "10",
        "吃饭了" to "好饿"
    )
    private val replyMap = mapOf(
        "炼" to "不许炼",
        "好累啊" to "累就对了",
        "涩涩" to "不可以涩涩",
        "不可以涩涩" to "不涩涩就挨打",
        "不准涩涩" to "不涩涩就挨打",
    )
    private val regexMap = mapOf<Regex, () -> Message>(
        Regex("^(睡觉|晚安).") to {
            if (now() in MIDNIGHT..of(6, 0)) Image("{1DB34403-D6DA-5C2C-C3D2-EF86C4D5E7CF}.gif")
            else PlainText("你这个年龄段你睡得着觉?")
        },
        Regex("^(泪目|哭了).*") to { PlainText("擦擦") }
    )

    override suspend fun handle(args: List<String>, event: GroupMessageEvent, isAt: Boolean): EventResult {
        val limiter = limiterMap[event.group.id] ?: return next()
        if (args.isEmpty()) return next()
        with(limiter) { if (!tryAcquire()) return next() }
        val (group, _, message) = event.getInfo()
        val arg = args[0]
        constMap[arg]?.let { group.sendMessage(it); return stop() }
        replyMap[arg]?.let { group.sendMessage(message.quote() + it); return stop() }
        regexMap.entries.find { it.key.matches(arg) }?.let { group.sendMessage(it.value()); return stop() }
        return next()
    }
}