package org.hff.miraiomnitrix.event.group

import com.google.common.util.concurrent.RateLimiter
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.config.BotProperties
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.db.entity.ReplyEnum
import org.hff.miraiomnitrix.db.service.AutoReplyService
import org.hff.miraiomnitrix.event.*
import org.hff.miraiomnitrix.utils.toImage
import org.springframework.boot.CommandLineRunner
import java.lang.management.ManagementFactory
import java.time.LocalTime
import java.util.concurrent.TimeUnit

@Event(priority = 5)
class AutoReply(
    private val permissionProperties: PermissionProperties,
    private val autoReplyService: AutoReplyService,
    private val botProperties: BotProperties,
) : GroupEvent, CommandLineRunner {

    val limiterMap = hashMapOf<Long, RateLimiter>()

    private lateinit var textMap: Map<String, List<String>>
    private lateinit var replyMap: Map<String, List<String>>
    private lateinit var imageMap: Map<String, List<Image>>
    private lateinit var regexImageMap: Map<Regex, List<Image>>

    private val regexMap = mapOf<String, () -> Message>(
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

    override suspend fun GroupMessageEvent.handle(args: List<String>, isAt: Boolean): EventResult {
        val arg = args.firstOrNull() ?: return stop()
        if (permissionProperties.replyExcludeGroup.contains(group.id)) return next()
        if (arg.startsWith(botProperties.name + "在吗")) return stop(getStatus().toPlainText())

        val limiter = limiterMap.getOrPut(group.id) { RateLimiter.create(0.025) }

        with(limiter) {
            when (arg) {
                in textMap.keys ->
                    textMap[arg]?.takeIf { tryAcquire() }?.run { return stop(random()) }

                in replyMap.keys ->
                    replyMap[arg]?.takeIf { tryAcquire() }?.run { return stop(message.quote() + random()) }

                in imageMap.keys ->
                    imageMap[arg]?.takeIf { tryAcquire() }?.run { return stop(random()) }

                else -> {
                    regexMap.entries.find { it.key.matches(arg) }?.value?.takeIf { tryAcquire() }
                        ?.run { return stop(invoke()) }
                    regexImageMap.entries.find { it.key.matches(arg) }?.value?.takeIf { tryAcquire() }
                        ?.run { return stop(random()) }
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
