package org.hff.miraiomnitrix.command.core

import com.google.common.cache.CacheBuilder
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.command.type.AnyCommand
import org.hff.miraiomnitrix.command.type.FriendCommand
import org.hff.miraiomnitrix.command.type.GroupCommand
import org.hff.miraiomnitrix.config.BotProperties
import org.hff.miraiomnitrix.exception.MyException
import org.hff.miraiomnitrix.handler.core.HandlerManger
import org.hff.miraiomnitrix.utils.SpringUtil.getBean
import org.hff.miraiomnitrix.utils.SpringUtil.getBeansWithAnnotation
import java.net.http.HttpTimeoutException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException
import kotlin.reflect.full.findAnnotation

object CommandManager {
    private val commandHeads = arrayOf("|", "\\", ",", ".", "，", "。")
    private val botProperties = getBean(BotProperties::class)

    private val anyCommands: HashMap<String, AnyCommand> = hashMapOf()
    private val friendCommands: HashMap<String, FriendCommand> = hashMapOf()
    private val groupCommands: HashMap<String, GroupCommand> = hashMapOf()
    private val noCommands: ArrayList<String> = arrayListOf()

    val errorCache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).build<Long, Exception>()

    init {
        getBeansWithAnnotation(Command::class)?.values?.forEach { command ->
            val annotation = command::class.findAnnotation<Command>()!!
            if (!annotation.isNeedHeader) annotation.name.forEach { noCommands.add(it) }
            when (command) {
                is AnyCommand -> annotation.name.forEach { anyCommands[it] = command }
                is FriendCommand -> annotation.name.forEach { friendCommands[it] = command }
                is GroupCommand -> annotation.name.forEach { groupCommands[it] = command }
            }
        }
    }

    suspend fun executeGroupCommand(sender: Member, message: MessageChain, group: Group) {
        val (commandName, args) = getCommandName(message) ?: return HandlerManger.groupHandle(message, group)
        val anyCommand = anyCommands[commandName]
        if (anyCommand != null) {
            return anyCommand.tryExecute(sender, message, group, args)
        }
        val groupCommand = groupCommands[commandName]
        if (groupCommand != null) {
            return groupCommand.tryExecute(sender, message, group, args)
        }
    }

    suspend fun executeFriendCommand(sender: Friend, message: MessageChain) {
        val (commandName, args) = getCommandName(message) ?: return HandlerManger.friendHandle(message, sender)
        val anyCommand = anyCommands[commandName]
        if (anyCommand != null) {
            return anyCommand.tryExecute(sender, message, sender, args)
        }
        val friendCommand = friendCommands[commandName]
        if (friendCommand != null) {
            return friendCommand.tryExecute(sender, message, args)
        }
    }

    private suspend fun AnyCommand.tryExecute(
        sender: User,
        message: MessageChain,
        subject: Contact,
        args: List<String>
    ) {
        try {
            val (msg, msgChain) = this.execute(sender, message, subject, args) ?: return
            subject.sendMessage(msg, msgChain)
        } catch (e: Exception) {
            sendCommandError(e, subject)
        }
    }

    private suspend fun GroupCommand.tryExecute(
        sender: Member,
        message: MessageChain,
        group: Group,
        args: List<String>
    ) {
        try {
            val (msg, msgChain) = this.execute(sender, message, group, args) ?: return
            group.sendMessage(msg, msgChain)
        } catch (e: Exception) {
            sendCommandError(e, group)
        }
    }

    private suspend fun FriendCommand.tryExecute(sender: Friend, message: MessageChain, args: List<String>) {
        try {
            val (msg, msgChain) = this.execute(sender, message, args) ?: return
            sender.sendMessage(msg, msgChain)
        } catch (e: Exception) {
            sendCommandError(e, sender)
        }
    }

    private suspend fun Contact.sendMessage(msg: String?, msgChain: MessageChain?) {
        if (msg != null) this.sendMessage(msg)
        if (msgChain != null) this.sendMessage(msgChain)
    }

    private suspend fun sendCommandError(e: Exception, subject: Contact) {
        errorCache.put(subject.id, e)
        when (e) {
            is SSLException -> subject.sendMessage("梯子挂了")
            is HttpTimeoutException -> subject.sendMessage("连接超时")
            is MyException -> subject.sendMessage(e.message)
            else -> subject.sendMessage("未知错误")
        }
    }

    private fun getCommandName(message: MessageChain): Pair<String, List<String>>? {
        var string = message.contentToString()
        var hasHead = commandHeads.any { string.startsWith(it) }
        if (hasHead) {
            string = string.substring(1)
        } else if (botProperties != null) {
            val atBot = "@" + botProperties.qq
            if (string.contains(atBot)) {
                hasHead = true
                string = string.replace(atBot, "")
            }
        }
        if (!hasHead) return null
        val args = string.trim().split(Regex("\\s+|\\[图片]"))
        return Pair(args[0], args.slice(1 until args.size))
    }

}
