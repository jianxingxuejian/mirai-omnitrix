package org.hff.miraiomnitrix.event.group

import com.google.common.util.concurrent.RateLimiter
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.db.entity.ReplyEnum
import org.hff.miraiomnitrix.db.service.AutoReplyService
import org.hff.miraiomnitrix.event.*
import org.hff.miraiomnitrix.utils.getInfo
import org.hff.miraiomnitrix.utils.sendImageAndCache
import org.hff.miraiomnitrix.utils.toImage
import org.springframework.boot.CommandLineRunner
import java.lang.management.ManagementFactory
import java.time.LocalTime
import java.util.concurrent.TimeUnit

@Event(priority = 2)
class AutoReply(
    private val permissionProperties: PermissionProperties,
    private val autoReplyService: AutoReplyService
) : GroupEvent, CommandLineRunner {

    val limiterMap = hashMapOf<Long, RateLimiter>()

    private lateinit var textMap: Map<String, List<String>>
    private lateinit var replyMap: Map<String, List<String>>
    private lateinit var imageMap: Map<String, List<Image>>
    private lateinit var regexImageMap: Map<Regex, List<Image>>

    private val regexMap = mapOf<String, () -> Message>(
        "(诺提拉斯|爱丽丝)在吗" to {
            getStatus().toPlainText()
        },
        ".*(睡觉|晚安).*" to {
            if (LocalTime.now() in LocalTime.MIDNIGHT..LocalTime.of(6, 0))
                "{1DB34403-D6DA-5C2C-C3D2-EF86C4D5E7CF}.gif".toImage()
            else "你这个年龄段你睡得着觉?".toPlainText()
        },
        ".*(泪目|哭了).*" to { "擦擦".toPlainText() },
    ).mapKeys { it.key.toRegex() }

    override fun run(vararg args: String?) = init()

    fun init() {
        val list = autoReplyService.list()
        textMap = list.filter { it.type == ReplyEnum.Text }.groupBy { it.keyword }
            .mapValues { entry -> entry.value.map { it.content } }
        replyMap = list.filter { it.type == ReplyEnum.Reply }.groupBy { it.keyword }
            .mapValues { entry -> entry.value.map { it.content } }
        imageMap = list.filter { it.type == ReplyEnum.Image }.groupBy { it.keyword }
            .mapValues { entry -> entry.value.map { it.content.toImage() } }
        regexImageMap = list.filter { it.type == ReplyEnum.Regex }.groupBy { it.keyword }
            .mapValues { entry -> entry.value.map { it.content.toImage() } }
            .mapKeys { it.key.toRegex() }
    }

    override suspend fun handle(args: List<String>, event: GroupMessageEvent, isAt: Boolean): EventResult {
        if (args.isEmpty()) return next()
        if (permissionProperties.replyExcludeGroup.contains(event.group.id)) return next()

        val limiter = limiterMap.getOrPut(event.group.id) { RateLimiter.create(0.05) }
        val (group, _, message) = event.getInfo()

        with(limiter) {
            when (val arg = args[0]) {
                in textMap.keys ->
                    textMap[arg]?.takeIf { tryAcquire() }?.run { return stop(this.random()) }

                in replyMap.keys ->
                    replyMap[arg]?.takeIf { tryAcquire() }?.run { return stop(message.quote() + this.random()) }

                in imageMap.keys ->
                    imageMap[arg]?.takeIf { tryAcquire() }?.run { group.sendImageAndCache(this.random());return stop() }

                else -> {
                    regexMap.entries.find { it.key.matches(arg) }?.value?.takeIf { tryAcquire() }
                        ?.run { return stop(this.invoke()) }
                    regexImageMap.entries.find { it.key.matches(arg) }?.value?.takeIf { tryAcquire() }
                        ?.run { group.sendImageAndCache(this.random());return stop() }
                }

            }
        }

        return next()
    }

    fun getStatus(): String {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val memory = "内存使用: ${totalMemory / 1024 / 1024}MB\n"
        val uptimeInMillis = ManagementFactory.getRuntimeMXBean().uptime
        val uptime = getUptime(uptimeInMillis)
        return memory + uptime
    }

    fun getUptime(time: Long): String {
        val days = TimeUnit.MILLISECONDS.toDays(time)
        val hours = TimeUnit.MILLISECONDS.toHours(time) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(time) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(time) % 60
        return "已运行: ${if (days > 0) "$days 天" else ""}${if (hours > 0) "$hours 小时 " else ""}${if (minutes > 0) "$minutes 分钟 " else ""}$seconds 秒"
    }

}
