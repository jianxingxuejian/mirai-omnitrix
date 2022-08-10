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
    private val anyCommands: HashMap<String, AnyCommand> = hashMapOf()
    private val friendCommands: HashMap<String, FriendCommand> = hashMapOf()
    private val groupCommands: HashMap<String, GroupCommand> = hashMapOf()
    private val noCommands: ArrayList<String> = arrayListOf()
    private val commandHeads = arrayOf(",", ".", "，", "。")
    private val botProperties = getBean(BotProperties::class.java)

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


    fun executeAnyCommand(sender: User, message: MessageChain, subject: Contact): MessageChain? {
        val (isCommand, args) = check(message.contentToString())
        val command = anyCommands[args[0]] ?: return null
        if (!isCommand) return null
        return command.execute(sender, message, subject, args.drop(1))
    }

    fun executeGroupCommand(sender: Member, message: MessageChain, group: Group): MessageChain? {
        val (isCommand, args) = check(message.contentToString())
        val command = groupCommands[args[0]] ?: return null
        if (!isCommand) return null
        return command.execute(sender, message, group, args.drop(1))
    }

    fun executeFriendCommand(sender: Friend, message: MessageChain): MessageChain? {
        val (isCommand, args) = check(message.contentToString())
        val command = friendCommands[args[0]] ?: return null
        if (!isCommand) return null
        return command.execute(sender, message, args.drop(1))
    }

    data class Result(val isCommand: Boolean, val args: List<String>)

    private fun check(message: String): Result {
        val (isCommand, msg) = commandHeads.any { message.startsWith(it) }
            .let {
                if (it) Pair(true, message.substring(1))
                else if (message.contains("@" + botProperties?.qq))
                    Pair(true, message.replace("@" + botProperties?.qq, ""))
                else Pair(false, message)
            }
        return Result(isCommand, msg.trim().split("\\s+"))
    }

}
