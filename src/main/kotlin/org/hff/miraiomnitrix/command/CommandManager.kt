package org.hff.miraiomnitrix.command

import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.command.any.AnyCommand
import org.hff.miraiomnitrix.command.friend.FriendCommand
import org.hff.miraiomnitrix.command.group.GroupCommand
import org.hff.miraiomnitrix.config.BotProperties
import org.hff.miraiomnitrix.utils.SpringUtil.getBean
import org.reflections.Reflections

object CommandManager {
    private val anyCommands: HashMap<String, AnyCommand> = TODO()
    private val friendCommands: HashMap<String, FriendCommand> = TODO()
    private val groupCommands: HashMap<String, GroupCommand> = TODO()
    private val commandHeads = arrayOf(",", ".", "/", "，", "。", "、")
    private val botProperties = getBean(BotProperties::class)

    init {
        val reflections = Reflections("org.hff.miraiomnitrix.command")
        val anyList = reflections.getSubTypesOf(AnyCommand::class.java).mapNotNull { it.kotlin.objectInstance }
        anyList.forEach { it.name?.forEach { name -> anyCommands[name] = it } }
        val friendList = reflections.getSubTypesOf(FriendCommand::class.java).mapNotNull { it.kotlin.objectInstance }
        friendList.forEach { it.name?.forEach { name -> friendCommands[name] = it } }
        val groupList = reflections.getSubTypesOf(GroupCommand::class.java).mapNotNull { it.kotlin.objectInstance }
        groupList.forEach { it.name?.forEach { name -> groupCommands[name] = it } }
    }


    fun executeAnyCommand(sender: User, message: MessageChain, subject: Contact): MessageChain? {
        val (isCommand, args) = check(message.contentToString())
        val command = anyCommands[args[0]] ?: return null
        if (!isCommand.and(command.isNeedHeader)) return null
        return command.execute(sender, message, subject, args.drop(1))
    }

    fun executeGroupCommand(sender: Member, message: MessageChain, group: Group): MessageChain? {
        val (isCommand, args) = check(message.contentToString())
        val command = groupCommands[args[0]] ?: return null
        if (!isCommand.and(command.isNeedHeader)) return null
        return command.execute(sender, message, group, args.drop(1))
    }

    fun executeFriendCommand(sender: Friend, message: MessageChain): MessageChain? {
        val (isCommand, args) = check(message.contentToString())
        val command = friendCommands[args[0]] ?: return null
        if (!isCommand.and(command.isNeedHeader)) return null
        return command.execute(sender, message, args.drop(1))
    }

    data class Result(val isCommand: Boolean, val args: List<String>)

    private fun check(message: String): Result {
        val (isCommand, msg) = commandHeads.any { message.startsWith(it) }
            .let {
                if (it) Pair(true, message.substring(1))
                else if (message.contains("@" + botProperties.qq))
                    Pair(true, message.replace("@" + botProperties.qq, ""))
                else Pair(false, message)
            }
        return Result(isCommand, msg.split("\\s+"))
    }
}
