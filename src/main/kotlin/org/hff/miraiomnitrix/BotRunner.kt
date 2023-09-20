package org.hff.miraiomnitrix

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.event.events.UserMessageEvent
import nu.pattern.OpenCV
import org.hff.miraiomnitrix.command.CommandManager
import org.hff.miraiomnitrix.command.any.isChatting
import org.hff.miraiomnitrix.common.messageCache
import org.hff.miraiomnitrix.common.putImage
import org.hff.miraiomnitrix.config.BotProperties
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import top.mrxiaom.qsign.QSignService
import xyz.cssxsh.mirai.tool.FixProtocolVersion
import java.io.File

lateinit var bot: Bot
lateinit var atBot: String

@Component
class BotRunner(private val botProperties: BotProperties) : CommandLineRunner {

    override fun run(args: Array<String>) {
        runBlocking(Dispatchers.IO) {
            val (qq, password) = botProperties
            if (qq == null || password == null) throw IllegalArgumentException("qq或者密码为空，请先在配置文件里添加")
            bot = BotFactory.newBot(qq, password) {
                protocol = botProperties.getProtocol()
                fileBasedDeviceInfo("device.json")
//                deviceInfo = MiraiDeviceGenerator()::load
            }
            atBot = "@" + bot.id

            // 加载opencv
            OpenCV.loadLocally()
            // 加载修复插件
            FixProtocolVersion.update()
            // 加载签名服务
            QSignService.Factory.apply {
                init(File("txlib/8.9.63"))
                loadProtocols()
                register()
            }

            bot.login()

            // 监听消息事件
            bot.eventChannel.subscribeAlways<MessageEvent> { handleMessage() }
            // 监听戳一戳事件
            bot.eventChannel.subscribeAlways<NudgeEvent> {
                // 机器人被戳后戳回去
                if (target.id == bot.id) from.nudge().sendTo(subject)
            }
        }
    }

    /** 处理消息，首先解析指令并执行，如果不是指令则接着进行事件处理 */
    private suspend inline fun MessageEvent.handleMessage() {
        putImage()
        when (this) {
            is GroupMessageEvent -> {
                if (isChatting()) return
                messageCache[group.id] = sender.id
                CommandManager.execute(this)
            }

            is UserMessageEvent -> {
                if (isChatting()) return
                CommandManager.execute(this)
            }

            else -> {}
        }
    }

}
