package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.event.events.MessageEvent
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.CommandResult
import org.hff.miraiomnitrix.command.result

@Command(name = ["menu", "help", "菜单", "帮助"])
class Menu : AnyCommand {

    private final val text = """
        |帮助说明：
        |一、指令功能(需要以中英文符号开头或者@机器人触发)：
        |1：`setu、涩图`，可追加：n[数量] r(r18) 任意标签(空格或者'|'分隔)
        |2：`bizhi、壁纸`，可追加：精选、横屏(pc)、竖屏(mp)、银发、兽耳、涩图
        |3：`bgm、番剧推荐`，使用二级关键词help获取详细帮助
        |4：`echo、复读`，复读后面的文字，可以伪造群里人的发言
        |5：`music、音乐`，网易云音乐分享
        |5：`live、直播`，直播状态查询
        |6：`chat、聊天`，与chatGPT机器人进行聊天
        |7：`chatplus、高级聊天`，更强大的chatGPT机器人
        |8：`ph、pornhub`，生成pornhub样式的图标
        |9：`红白、5000、choyen`，生成choyen5000样式图片
        |10：`waifu、老婆`，与随机群员结婚，可以@指定对象，可以使用原神|崩坏三|碧蓝航线|碧蓝档案|明日方舟|公主连结角色名，可以自定义图片与角色名
        |11：`mute、禁言`，随机禁言0-600秒
        |12：`js`，执行js程序
        |二、非指令功能
        |1: `st、搜图`，使用关键字回复一张图片，或者与自己的图片一起发送，得到saucenao搜图结果
        |2：`爬、一直、急急国王`，生成表情包
        |3：B站链接解析
        |4：合成两个emoji
        |5：二刺螈词库
    """.trimMargin()
    private final val resultMsg = result(text)

    override suspend fun execute(args: List<String>, event: MessageEvent): CommandResult? = resultMsg

}
