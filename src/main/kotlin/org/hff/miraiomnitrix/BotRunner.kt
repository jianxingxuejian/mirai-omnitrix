package org.hff.miraiomnitrix

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.utils.BotConfiguration
import org.hff.miraiomnitrix.config.BotProperties
import org.hff.miraiomnitrix.listener.MyListener
import org.hff.miraiomnitrix.utils.Util.bot
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class BotRunner(private val botProperties: BotProperties) : CommandLineRunner {

    override fun run(args: Array<String>) {
        runBlocking {
            val (qq, password) = botProperties
            if (qq == null || password == null) {
                println("qq或者密码为空，请先在配置文件里添加")
                return@runBlocking
            }
            bot = BotFactory.newBot(qq, password) {
                protocol = BotConfiguration.MiraiProtocol.IPAD
                fileBasedDeviceInfo("device.json")
            }
            bot.login()
            bot.eventChannel.registerListenerHost(MyListener)
        }
    }
}