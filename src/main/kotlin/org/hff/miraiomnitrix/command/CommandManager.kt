package org.hff.miraiomnitrix.command

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.events.UserMessageEvent
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import org.hff.miraiomnitrix.atBot
import org.hff.miraiomnitrix.command.any.ChatPlus
import org.hff.miraiomnitrix.command.any.Tts
import org.hff.miraiomnitrix.common.check
import org.hff.miraiomnitrix.common.errorCache
import org.hff.miraiomnitrix.common.sendAndCache
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.event.EventManger
import org.hff.miraiomnitrix.utils.SpringUtil
import kotlin.reflect.full.findAnnotation

object CommandManager {
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

    val ttsCommand = getCommand<Tts>()


    /** 指令头 */
    private val commandHeads = hashSetOf('|', '\\', ',', '.', '，', '。')
    private val chatPlusInclude = SpringUtil.getBean(PermissionProperties::class).chatPlusNotNeedCommandGroup
    private val chatPlusCommand = getCommand<ChatPlus>()

    /** 解析群消息文本并执行指令，如果不是指令，则会继续执行事件 */
    suspend fun execute(event: GroupMessageEvent) {
        // 解析原始文本，判断是否有指令头或者@机器人
        val (content, hasHead, isAt) = event.message.contentToString().trim().run {
            if (commandHeads.contains(first())) Triple(substring(1), true, false)
            else if (startsWith(atBot)) Triple(replace(atBot, ""), false, true)
            else Triple(this, false, false)
        }
        val args = content.toArgs()
        // 配置了无需指令头就能使用chatplus功能的群，直接执行，该功能可能跟其他指令会产生冲突
        if (isAt && chatPlusInclude.contains(event.group.id)) {
            chatPlusCommand.tryExecute(args, event)
            return
        }
        val check = hasHead || isAt
        val commandName = args[0]
        val appendArgs = args.drop(1)
        groupCommands[commandName]?.run {
            if (needHead && !check) return
            tryExecute(appendArgs, event)
        } ?: anyCommands[commandName]?.run {
            if (needHead && !check) return
            tryExecute(appendArgs, event)
        } ?: EventManger.handle(args, event, isAt)
    }

    /** 解析用户消息文本并执行指令，如果不是指令，则会继续执行事件 */
    suspend fun execute(event: UserMessageEvent) {
        val args = event.message.contentToString().trim().run {
            if (commandHeads.contains(first())) substring(1)
            else this
        }.toArgs()
        val commandName = args[0]
        val appendArgs = args.drop(1)
        userCommands[commandName]?.apply { if (!check(event)) return }?.tryExecute(appendArgs, event)
            ?: anyCommands[commandName]?.apply { if (!check(event)) return }?.tryExecute(appendArgs, event)
            ?: EventManger.handle(args, event)
    }

    /** 字符串转换成参数列表 */
    private fun String.toArgs() = replace("[图片]", "").replace("[动画表情]", "").trim().split(Regex("\\s+"))

    private suspend inline fun <reified T : MessageEvent> Execute<T>.tryExecute(args: List<String>, event: T) {
        event.run {
            try {
                execute(args)?.run { subject.sendAndCache(this) }
            } catch (e: Exception) {
                e.printStackTrace()
                errorCache.put(subject.id, e)
                subject.sendAndCache(message.quote() + "指令执行失败，错误信息：" + (e.message ?: "未知错误"))
            }
        }
    }

    /** 获取指令实例 */
    private inline fun <reified T : Execute<MessageEvent>> getCommand(): T {
        val name = T::class.simpleName!!.lowercase()
        return (anyCommands[name] ?: groupCommands[name] ?: userCommands[name]) as T
    }

}
