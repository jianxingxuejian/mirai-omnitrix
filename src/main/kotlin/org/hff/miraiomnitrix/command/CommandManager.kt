package org.hff.miraiomnitrix.command

import com.google.common.cache.CacheBuilder
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.command.any.AnyCommand
import org.hff.miraiomnitrix.command.friend.FriendCommand
import org.hff.miraiomnitrix.command.group.GroupCommand
import org.hff.miraiomnitrix.config.BotProperties
import org.hff.miraiomnitrix.exception.MyException
import org.hff.miraiomnitrix.utils.SpringUtil
import org.hff.miraiomnitrix.utils.Util.getInfo
import java.net.http.HttpTimeoutException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException
import kotlin.reflect.full.findAnnotation

/**
 * 指令管理器，设置指令头(硬编码)，加载所有指令
 *
 * TODO: 指令头可配置
 */
object CommandManager {
    private val commandHeads = arrayOf("|", "\\", ",", ".", "，", "。")
    private val botProperties = SpringUtil.getBean(BotProperties::class)

    private val anyCommands: HashMap<String, AnyCommand> = hashMapOf()
    private val friendCommands: HashMap<String, FriendCommand> = hashMapOf()
    private val groupCommands: HashMap<String, GroupCommand> = hashMapOf()

    private val errorCache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).build<Long, Exception>()

    /** 加载Command注解下的所有指令 */
    init {
        SpringUtil.getBeansWithAnnotation(Command::class)?.values?.forEach { command ->
            val annotation = command::class.findAnnotation<Command>()
            when (command) {
                is AnyCommand -> annotation?.name?.forEach { anyCommands[it] = command }
                is FriendCommand -> annotation?.name?.forEach { friendCommands[it] = command }
                is GroupCommand -> annotation?.name?.forEach { groupCommands[it] = command }
            }
        }
        if (anyCommands.isEmpty() && friendCommands.isEmpty() && groupCommands.isEmpty()) {
            throw RuntimeException("指令加载失败")
        }
    }

    /** 处理消息，如果是指令则执行 */
    suspend fun handle(event: MessageEvent): Pair<Boolean, List<String>> {
        val (commandName, args) = getCommandName(event.message)
        if (commandName == null) return Pair(false, args)
        val anyCommand = anyCommands[commandName]
        if (anyCommand != null) {
            anyCommand.tryExecute(args, event)
            return Pair(true, args)
        }
        when (event) {
            is GroupMessageEvent -> {
                val groupCommand = groupCommands[commandName] ?: return Pair(false, args)
                groupCommand.tryExecute(args, event)
                return Pair(true, args)
            }

            is FriendMessageEvent -> {
                val friendCommand = friendCommands[commandName] ?: return Pair(false, args)
                friendCommand.tryExecute(args, event)
                return Pair(true, args)

            }

            else -> {}
        }
        return Pair(false, listOf(commandName))
    }

    private suspend fun AnyCommand.tryExecute(args: List<String>, event: MessageEvent) {
        val (sender, message, subject) = getInfo(event)
        try {
            val (msg, msgChain) = this.execute(sender, message, subject, args, event) ?: return
            subject.sendMessage(msg, msgChain)
        } catch (e: Exception) {
            sendCommandError(e, subject)
        }
    }

    private suspend fun GroupCommand.tryExecute(args: List<String>, event: GroupMessageEvent) {
        val (sender, message, group) = getInfo(event)
        try {
            val (msg, msgChain) = this.execute(sender, message, group, args, event) ?: return
            group.sendMessage(msg, msgChain)
        } catch (e: Exception) {
            sendCommandError(e, group)
        }
    }

    private suspend fun FriendCommand.tryExecute(args: List<String>, event: FriendMessageEvent) {
        val (friend, message) = getInfo(event)
        try {
            val (msg, msgChain) = this.execute(friend, message, args, event) ?: return
            friend.sendMessage(msg, msgChain)
        } catch (e: Exception) {
            sendCommandError(e, friend)
        }
    }

    private suspend fun Contact.sendMessage(msg: String?, msgChain: MessageChain?) {
        if (msg != null) this.sendMessage(msg)
        if (msgChain != null) this.sendMessage(msgChain)
    }

    private suspend fun sendCommandError(e: Exception, subject: Contact) {
        e.printStackTrace()
        errorCache.put(subject.id, e)
        when (e) {
            is SSLException -> subject.sendMessage("梯子挂了")
            is HttpTimeoutException -> subject.sendMessage("连接超时")
            is MyException -> subject.sendMessage(e.message)
            else -> subject.sendMessage("未知错误")
        }
    }

    /**
     * 解析消息文本，判断是否含有指令头或者@机器人
     *
     * @param message 原始消息链
     * @param needHead 是否需要指令头，默认值为true，如果是好友消息之类的则无需指令头
     * @return Pair<指令名, 参数列表>
     */
    private fun getCommandName(message: MessageChain, needHead: Boolean = true): Pair<String?, List<String>> {
        var string = message.contentToString()
        var hasHead = commandHeads.any { string.startsWith(it) }
        if (needHead) {
            if (hasHead) {
                string = string.substring(1)
            } else if (botProperties != null) {
                val atBot = "@" + botProperties.qq
                if (string.contains(atBot)) {
                    hasHead = true
                    string = string.replace(atBot, "")
                }
            }
        }
        val args = string.replace("[图片]", "").replace("[动画表情]", "").trim().split(Regex("\\s+"))
        if (needHead && !hasHead) return Pair(null, args)
        return Pair(args[0], args.slice(1 until args.size))
    }

}
