package org.hff.miraiomnitrix.command

import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.command.any.AnyCommand
import org.hff.miraiomnitrix.command.friend.FriendCommand
import org.hff.miraiomnitrix.command.group.GroupCommand
import org.hff.miraiomnitrix.config.BotProperties
import org.hff.miraiomnitrix.utils.SpringUtil.getBean
import org.hff.miraiomnitrix.utils.SpringUtil.getBeansWithAnnotation
import kotlin.reflect.full.findAnnotation

object CommandManager {
    private val commandHeads = arrayOf(",", ".", "，", "。")
    private val botProperties = getBean(BotProperties::class.java)

    private val anyCommands: HashMap<String, AnyCommand> = hashMapOf()
    private val friendCommands: HashMap<String, FriendCommand> = hashMapOf()
    private val groupCommands: HashMap<String, GroupCommand> = hashMapOf()
    private val noCommands: ArrayList<String> = arrayListOf()

    init {
        getBeansWithAnnotation(Command::class.java)?.values?.forEach { command ->
            val annotation = command::class.findAnnotation<Command>()!!
            if (!annotation.isNeedHeader) annotation.name.forEach { noCommands.add(it) }
            when (command) {
                is AnyCommand -> annotation.name.forEach { anyCommands[it] = command }
                is FriendCommand -> annotation.name.forEach { friendCommands[it] = command }
                is GroupCommand -> annotation.name.forEach { groupCommands[it] = command }
            }
        }
    }

    suspend fun executeAnyCommand(sender: User, message: MessageChain, subject: Contact): MessageChain? {
        val (command, args) = getCommand(message, anyCommands) ?: return null
        return command.execute(sender, message, subject, args)
    }

    suspend fun executeGroupCommand(sender: Member, message: MessageChain, group: Group): MessageChain? {
        val (command, args) = getCommand(message, groupCommands) ?: return null
        return command.execute(sender, message, group, args)
    }

    suspend fun executeFriendCommand(sender: Friend, message: MessageChain): MessageChain? {
        val (command, args) = getCommand(message, friendCommands) ?: return null
        return command.execute(sender, message, args)
    }

    private fun <T> getCommand(message: MessageChain, commands: HashMap<String, T>): Pair<T,MutableList<String>>? {
        val string = message.contentToString()
        val (hasHeader, msg) = commandHeads.any { string.startsWith(it) }
            .let {
                if (it) Pair(true, string.substring(1))
                else if (botProperties != null && string.contains("@" + botProperties.qq))
                    Pair(true, string.replace("@" + botProperties.qq, ""))
                else Pair(false, string)
            }
        val args = msg.trim().split("\\s+").toMutableList()
        for ((index, arg) in args.withIndex()) {
            if (!noCommands.contains(arg) && !hasHeader) continue
            val command = commands[arg] ?: continue
            args.removeAt(index)
            return Pair(command, args)
        }
        return null
    }

}
