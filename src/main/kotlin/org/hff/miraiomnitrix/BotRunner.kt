package org.hff.miraiomnitrix

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.utils.BotConfiguration
import org.hff.miraiomnitrix.command.CommandManager
import org.hff.miraiomnitrix.config.BotProperties
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import kotlin.coroutines.CoroutineContext

@Component
class BotRunner(private val botProperties: BotProperties) : CommandLineRunner {

    companion object {
        lateinit var bot: Bot
    }

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

    object MyListener : SimpleListenerHost() {

        override fun handleException(context: CoroutineContext, exception: Throwable) {
            exception.printStackTrace()
        }

        @EventHandler
        suspend fun GroupMessageEvent.onMessage() = CommandManager.executeGroupCommand(
            sender, message, group, this
        )

        @EventHandler
        suspend fun FriendMessageEvent.onMessage() = CommandManager.executeFriendCommand(sender, message, this)
    }
}