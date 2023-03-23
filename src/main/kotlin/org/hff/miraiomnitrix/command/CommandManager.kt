package org.hff.miraiomnitrix.command

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.events.UserMessageEvent
import org.hff.miraiomnitrix.BotRunner
import org.hff.miraiomnitrix.common.check
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.event.any.Cache
import org.hff.miraiomnitrix.utils.SpringUtil
import kotlin.reflect.full.findAnnotation

/**
 * 指令管理器，设置指令头(硬编码)，加载所有指令
 *
 * TODO: 指令头可配置
 */
object CommandManager {
    private val commandHeads = arrayOf("|", "\\", ",", ".", "，", "。")
    private val permissionProperties = SpringUtil.getBean(PermissionProperties::class)

    /** 指令容器 */
    val anyCommands: HashMap<String, AnyCommand> = hashMapOf()
    private val groupCommands: HashMap<String, GroupCommand> = hashMapOf()
    private val userCommands: HashMap<String, UserCommand> = hashMapOf()

    /** 加载Command注解下的所有指令 */
    init {
        SpringUtil.getBeansWithAnnotation(Command::class)?.values?.forEach { command ->
            val annotation = command::class.findAnnotation<Command>()
            when (command) {
                is AnyCommand -> annotation?.name?.forEach { anyCommands[it] = command }
                is UserCommand -> annotation?.name?.forEach { userCommands[it] = command }
                is GroupCommand -> annotation?.name?.forEach { groupCommands[it] = command }
            }
        }
    }

    /** 处理消息，如果是指令则执行 */
    suspend fun handle(event: MessageEvent): Triple<Boolean, List<String>, Boolean> = when (event) {
        is GroupMessageEvent -> {
            val (commandName, args, isAt) = parseCommand(event)
            if (commandName == null) Triple(false, args, isAt)
            else groupCommands[commandName]?.tryExecute(args, event)
                ?: anyCommands[commandName]?.tryExecute(args, event)
                ?: Triple(false, listOf(commandName), isAt)
        }

        is UserMessageEvent -> {
            val (commandName, args, isAt) = parseCommand(event, false)
            if (commandName == null) Triple(false, args, isAt)
            else userCommands[commandName]?.takeIf { it.check(event) }?.tryExecute(args, event)
                ?: anyCommands[commandName]?.takeIf { it.check(event) }?.tryExecute(args, event)
                ?: Triple(false, listOf(commandName), isAt)
        }

        else -> Triple(false, listOf(), false)
    }

    private suspend fun <T : MessageEvent> Execute<T>.tryExecute(
        args: List<String>,
        event: T
    ): Triple<Boolean, List<String>, Boolean> {
        val subject = event.subject
        try {
            this.execute(args, event)?.run {
                subject.sendMessage(this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Cache.errorCache.put(subject.id, e)
            subject.sendMessage(e.message ?: "未知错误")
        }
        return Triple(true, args, true)
    }


    /**
     * 解析消息文本，判断是否含有指令头或者@机器人
     *
     * @param event 原始事件
     * @param needHead 是否需要指令头，默认值为true，如果是非群消息则无需指令头
     * @return Pair<指令名, 参数列表>
     */
    private fun parseCommand(event: MessageEvent, needHead: Boolean = true): Triple<String?, List<String>, Boolean> {
        var string = event.message.contentToString().trim()

        var hasHead = commandHeads.any { string.startsWith(it) }
        var isAt = false
        if (needHead) {
            if (hasHead) {
                string = string.substring(1)
            } else {
                val atBot = "@" + BotRunner.bot.id
                if (string.contains(atBot)) {
                    hasHead = true
                    isAt = true
                    string = string.replace(atBot, "")
                    // 如果是群消息，且群号在不需要chatplus指令头的群号列表中
                    if (permissionProperties.chatPlusNotNeedCommandGroup.contains(event.subject.id)) {
                        return Triple("chatplus", string.split(Regex("\\s+")), true)
                    }
                }
            }
        }
        val args = string.replace("[图片]", "").replace("[动画表情]", "").trim().split(Regex("\\s+"))
        if (needHead && !hasHead) return Triple(null, args, false)
        return Triple(args[0], args.slice(1 until args.size), isAt)
    }

    inline fun <reified T : AnyCommand> getCommand(): T = anyCommands[T::class.simpleName!!.lowercase()] as T

}
