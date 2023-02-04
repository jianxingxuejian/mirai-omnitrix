package org.hff.miraiomnitrix.command.impl.any

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.command.core.Command
import org.hff.miraiomnitrix.command.type.AnyCommand
import org.hff.miraiomnitrix.result.ResultMessage
import org.hff.miraiomnitrix.result.result

@Command(name = ["menu", "help", "菜单", "帮助"])
class Menu : AnyCommand {

    private final val text = """
        |帮助说明：
        |一、指令功能(需要以中英文符号开头或者@机器人触发)：
        |1：{ setu、涩图 }，可追加：n[数量] r(r18) 任意标签(空格或者'|'分隔)
        |2：{ bizhi、壁纸 }，可追加：精选、横屏(pc)、竖屏(mp)、银发、兽耳、涩图
        |3：{ bgm、番剧推荐 }，bangumi番剧随机推荐，可追加：年份(4位数字) r[最低排名] n[数量] 任意关键词
        |4：{ echo、复读 }，复读后面的文字，可以伪造群里人的发言
        |5：{ live、直播 }，直播状态查询
        |6：{ chat、聊天 }，与openai机器人进行聊天
        |7：{ js }，执行js程序
        |二、非指令功能
        |1: { st、搜图 }，使用关键字回复一张图片，或者与自己的图片一起发送，得到saucenao搜图结果
        |2：{ 爬、一直、急急国王 }，生成表情包
    """.trimMargin()
    private final val result = result(text)

    override suspend fun execute(
        sender: User,
        message: MessageChain,
        subject: Contact,
        args: List<String>,
        event: MessageEvent
    ): ResultMessage? {
        return result
    }

}