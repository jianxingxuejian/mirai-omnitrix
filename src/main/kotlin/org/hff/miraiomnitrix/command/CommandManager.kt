package org.hff.miraiomnitrix.command

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.events.UserMessageEvent
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.common.check
import org.hff.miraiomnitrix.config.BotProperties
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
    private val botProperties = SpringUtil.getBean(BotProperties::class)

    /** 指令容器 */
    private val anyCommands: HashMap<String, AnyCommand> = hashMapOf()
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
    suspend fun handle(event: MessageEvent): Pair<Boolean, List<String>> {
        val (commandName, args) = getCommandName(event.message)
        if (commandName == null) return Pair(false, args)

        val result = when (event) {
            is GroupMessageEvent -> groupCommands[commandName]?.tryExecute(args, event)
                ?: anyCommands[commandName]?.tryExecute(args, event)

            is UserMessageEvent -> userCommands[commandName]?.takeIf { it.check(event) }?.tryExecute(args, event)
                ?: anyCommands[commandName]?.takeIf { it.check(event) }?.tryExecute(args, event)

            else -> null
        }
        return result ?: Pair(false, listOf(commandName))
    }

    private suspend fun <T : MessageEvent> Execute<T>.tryExecute(
        args: List<String>,
        event: T
    ): Pair<Boolean, List<String>> {
        val subject = event.subject
        try {
            this.execute(args, event)?.let { (msg, msgChain) ->
                msg?.let { subject.sendMessage(it) }
                msgChain?.let { subject.sendMessage(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Cache.errorCache.put(subject.id, e)
            subject.sendMessage(e.message ?: "未知错误")
        }
        return Pair(true, args)
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
