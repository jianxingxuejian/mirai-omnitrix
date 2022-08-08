package org.hff.miraiomnitrix

import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.utils.BotConfiguration
import org.hff.miraiomnitrix.config.BotProperties
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class BotRunner(val botProperties: BotProperties) : CommandLineRunner {

    override fun run(args: Array<String>) {
        val bot = BotFactory.newBot(botProperties.qq, botProperties.password) {
            protocol = BotConfiguration.MiraiProtocol.IPAD
            fileBasedDeviceInfo("device.json")
        }
//        bot.login()
    }
}