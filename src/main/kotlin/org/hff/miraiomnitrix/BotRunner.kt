package org.hff.miraiomnitrix

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.utils.BotConfiguration
import org.hff.miraiomnitrix.config.BotProperties
import org.hff.miraiomnitrix.listener.AnyListener
import org.hff.miraiomnitrix.listener.FriendListener
import org.hff.miraiomnitrix.listener.GroupListener
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class BotRunner(private val botProperties: BotProperties) : CommandLineRunner {

    override fun run(args: Array<String>) {
        runBlocking {
            val (qq, password) = botProperties
            val bot = BotFactory.newBot(qq, password) {
                BotConfiguration.MiraiProtocol.IPAD
                fileBasedDeviceInfo("device.json")
            }
            bot.login()
            bot.eventChannel.registerListenerHost(GroupListener)
            bot.eventChannel.registerListenerHost(FriendListener)
            bot.eventChannel.registerListenerHost(AnyListener)
        }
    }
}