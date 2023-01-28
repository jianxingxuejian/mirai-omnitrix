package org.hff.miraiomnitrix.command.core

import com.google.common.cache.CacheBuilder
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.command.type.AnyCommand
import org.hff.miraiomnitrix.command.type.FriendCommand
import org.hff.miraiomnitrix.command.type.GroupCommand
import org.hff.miraiomnitrix.config.BotProperties
import org.hff.miraiomnitrix.exception.MyException
import org.hff.miraiomnitrix.result.ResultMessage
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

    suspend fun executeAnyCommand(sender: User, message: MessageChain, subject: Contact): ResultMessage? {
        val (command, args) = if (subject is Friend) {
            getCommandByFriend(message, anyCommands) ?: return null
        } else {
            getCommand(message, anyCommands) ?: return null
        }
        return command.tryExecute(sender, message, subject, args)
    }

    suspend fun executeGroupCommand(sender: Member, message: MessageChain, group: Group): ResultMessage? {
        val (command, args) = getCommand(message, groupCommands) ?: return null
        return command.tryExecute(sender, message, group, args)
    }

    suspend fun executeFriendCommand(sender: Friend, message: MessageChain): ResultMessage? {
        val (command, args) = getCommandByFriend(message, friendCommands) ?: return null
        return command.tryExecute(sender, message, args)
    }

    private suspend fun AnyCommand.tryExecute(
        sender: User,
        message: MessageChain,
        subject: Contact,
        args: List<String>
    ) = try {
        this.execute(sender, message, subject, args)
    } catch (e: Exception) {
        sendCommandError(e, subject)
    }

    private suspend fun GroupCommand.tryExecute(
        sender: Member,
        message: MessageChain,
        group: Group,
        args: List<String>
    ) = try {
        this.execute(sender, message, group, args)
    } catch (e: Exception) {
        sendCommandError(e, group)
    }

    private suspend fun FriendCommand.tryExecute(sender: Friend, message: MessageChain, args: List<String>) = try {
        this.execute(sender, message, args)
    } catch (e: Exception) {
        sendCommandError(e, sender)
    }

    private suspend fun sendCommandError(e: Exception, subject: Contact): Nothing? {
        errorCache.put(subject.id, e)
        when (e) {
            is SSLException -> subject.sendMessage("梯子挂了")
            is HttpTimeoutException -> subject.sendMessage("连接超时")
            is MyException -> subject.sendMessage(e.message)
            else -> subject.sendMessage("未知错误")
        }
        return null
    }


    private fun <T> getCommand(message: MessageChain, commands: HashMap<String, T>): Pair<T, MutableList<String>>? {
        val string = message.contentToString()
        val (hasHeader, msg) = commandHeads.any { string.startsWith(it) }
            .let {
                if (it) Pair(true, string.substring(1))
                else if (botProperties != null && string.contains("@" + botProperties.qq))
                    Pair(true, string.replace("@" + botProperties.qq, ""))
                else Pair(false, string)
            }
        val args = msg.trim().split(Regex("\\s+|\\[图片]")).toMutableList()
        for ((index, arg) in args.withIndex()) {
            if (!noCommands.contains(arg) && !hasHeader) continue
            val command = commands[arg] ?: continue
            args.removeAt(index)
            return Pair(command, args)
        }
        return null
    }

    private fun <T> getCommandByFriend(
        message: MessageChain,
        commands: HashMap<String, T>
    ): Pair<T, MutableList<String>>? {
        val string = message.contentToString()
        val args = string.trim().split(Regex("\\s+|\\[图片]")).toMutableList()
        for ((index, arg) in args.withIndex()) {
            val command = commands[arg] ?: continue
            args.removeAt(index)
            return Pair(command, args)
        }
        return null
    }

}
