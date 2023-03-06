package org.hff.miraiomnitrix.command

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.events.UserMessageEvent
import org.hff.miraiomnitrix.common.check
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.event.any.Cache
import org.hff.miraiomnitrix.utils.SpringUtil
import org.hff.miraiomnitrix.utils.Util
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
    suspend fun handle(event: MessageEvent): Pair<Boolean, List<String>> = when (event) {
        is GroupMessageEvent -> {
            val (commandName, args) = getCommandName(event)
            if (commandName == null) Pair(false, args)
            else groupCommands[commandName]?.tryExecute(args, event)
                ?: anyCommands[commandName]?.tryExecute(args, event)
                ?: Pair(false, listOf(commandName))
        }

        is UserMessageEvent -> {
            val (commandName, args) = getCommandName(event, false)
            if (commandName == null) Pair(false, args)
            else userCommands[commandName]?.takeIf { it.check(event) }?.tryExecute(args, event)
                ?: anyCommands[commandName]?.takeIf { it.check(event) }?.tryExecute(args, event)
                ?: Pair(false, listOf(commandName))
        }

        else -> Pair(false, listOf())
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
     * @param event 原始事件
     * @param needHead 是否需要指令头，默认值为true，如果是非群消息则无需指令头
     * @return Pair<指令名, 参数列表>
     */
    private fun getCommandName(event: MessageEvent, needHead: Boolean = true): Pair<String?, List<String>> {
        var string = event.message.contentToString().trim()

        var hasHead = commandHeads.any { string.startsWith(it) }
        if (needHead) {
            if (hasHead) {
                string = string.substring(1)
            } else {
                val atBot = Util.atBot()
                if (string.contains(atBot)) {
                    hasHead = true
                    string = string.replace(atBot, "")
                    // 如果是群消息，且群号在不需要chatplus指令头的群号列表中
                    permissionProperties?.chatPlusNotNeedCommandGroup?.let { list ->
                        if (list.isNotEmpty() && list.contains(event.subject.id)) {
                            return Pair("chatplus", string.split(Regex("\\s+")))
                        }
                    }
                }
            }
        }
        val args = string.replace("[图片]", "").replace("[动画表情]", "").trim().split(Regex("\\s+"))
        if (needHead && !hasHead) return Pair(null, args)
        return Pair(args[0], args.slice(1 until args.size))
    }

}
