package org.hff.miraiomnitrix

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.utils.BotConfiguration
import org.hff.miraiomnitrix.command.CommandManager
import org.hff.miraiomnitrix.config.BotProperties
import org.hff.miraiomnitrix.event.EventManger
import org.springframework.boot.CommandLineRunner
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component
import xyz.cssxsh.mirai.tool.FixProtocolVersion

@Component
@EnableScheduling
class BotRunner(private val botProperties: BotProperties) : CommandLineRunner {

    companion object {
        lateinit var bot: Bot
    }

    override fun run(args: Array<String>) {
        runBlocking(Dispatchers.IO) {
            val (qq, password) = botProperties
            if (qq == null || password == null) throw IllegalArgumentException("qq或者密码为空，请先在配置文件里添加")
            FixProtocolVersion.update()
            bot = BotFactory.newBot(qq, password) {
                protocol = BotConfiguration.MiraiProtocol.MACOS
                fileBasedDeviceInfo("device.json")
            }
            bot.login()
            // 监听所有消息
            bot.eventChannel.subscribeAlways<MessageEvent> { handleMessage(this) }
            // 机器人被戳后戳回去
            bot.eventChannel.subscribeAlways<NudgeEvent> {
                if (target.id == bot.id) from.nudge().sendTo(subject)
            }
        }
    }

    /** 随机戳一个群友 */
//    @Scheduled(initialDelay = 60_000, fixedDelay = 90_000)
//    fun nudge() {
//        runBlocking {
//            val group = BotRunner.bot.groups.random()
//            val member = group.members.random()
//            member.nudge().sendTo(group)
//        }
//    }

}

/** 处理消息，首先解析指令并执行，如果不是指令则接着进行事件处理 */
suspend fun handleMessage(event: MessageEvent) {
    val (execute, args, isAt) = CommandManager.handle(event)
    if (execute) return
    EventManger.handle(args, event, isAt)
}
