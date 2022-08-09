package org.hff.miraiomnitrix

import org.hff.miraiomnitrix.command.any.AnyCommand
import org.hff.miraiomnitrix.config.BotProperties
import org.reflections.Reflections
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class BotRunner(val botProperties: BotProperties) : CommandLineRunner {

    override fun run(args: Array<String>) {
//        runBlocking {
//            val bot = BotFactory.newBot(botProperties.qq, botProperties.password) {
//                BotConfiguration.MiraiProtocol.IPAD
//                fileBasedDeviceInfo("device.json")
//            }
//            bot.login()
//            bot.eventChannel.registerListenerHost(GroupListener)
//        }
        val reflections = Reflections("org.hff.miraiomnitrix.command.any")
        val list = reflections.getSubTypesOf(AnyCommand::class.java).mapNotNull { it.kotlin.objectInstance }
        list[0].name.forEach { println(it) }
    }
}