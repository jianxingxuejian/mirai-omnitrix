package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.event.events.MessageEvent
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.CommandResult
import org.hff.miraiomnitrix.utils.SkiaExternalResource
import org.hff.miraiomnitrix.utils.SkikoUtil
import org.jetbrains.skia.*

@Command(name = ["menu", "help", "菜单", "帮助"])
class Menu : AnyCommand {

    private final val text = """
        |帮助说明：
        |一、指令功能(需要以中英文符号开头或者@机器人触发)：
        |1：setu、涩图，可追加：n[数量] r(r18) 任意标签(空格或者'|'分隔)
        |2：bizhi、壁纸，可追加：精选、横屏(pc)、竖屏(mp)、银发、兽耳、涩图
        |3：bgm、番剧推荐，使用二级关键词help获取详细帮助
        |4：echo、复读，复读后面的文字，可以伪造群里人的发言
        |5：music、音乐，网易云音乐分享
        |5：live、直播，直播状态查询
        |6：chat、聊天，与chatGPT机器人进行聊天
        |7：chatplus、高级聊天，更强大的chatGPT机器人
        |8：ph、pornhub，生成pornhub样式的图标
        |9：红白、5000、choyen，生成choyen5000样式图片
        |10：waifu、老婆，与随机群员结婚，可以@指定对象，可以自定义图片与角色名
        |    可以使用原神|崩坏三|碧蓝航线|碧蓝档案|明日方舟|公主连结角色名
        |11：mute、禁言，随机禁言0-600秒
        |12：js，执行js程序
        |二、非指令功能
        |1：st、搜图，使用关键字回复一张图片，或者与自己的图片一起发送
        |2：爬、一直、急急国王，生成表情包
        |3：B站链接解析
        |4：合成两个emoji
        |5：二刺螈词库
        |开源地址：https://github.com/jianxingxuejian/mirai-omnitrix
    """.trimMargin()

    private val menu = convertImage(text)

    override suspend fun execute(args: List<String>, event: MessageEvent): CommandResult? {
        event.subject.sendImage(menu)
        return null
    }

    private final fun convertImage(text: String): SkiaExternalResource {
        val bgColor = 0xFF2C3E50.toInt()
        val textColor = 0xFFE7E7E7.toInt()
        val keywordColor = 0xFF2ECC71.toInt()
        val fontSize = 20F
        val font = SkikoUtil.getFont("SimSun", FontStyle.NORMAL, fontSize)
        val lineHeight = 1.6F
        val paddingX = 15F
        val paddingY = 10F
        val width = 800
        val lines = text.split("\n")
        val height = lines.size * fontSize * lineHeight + 2 * paddingY
        val surface = Surface.makeRasterN32Premul(width, height.toInt())
        with(surface.canvas) {
            val paint = Paint().apply { isAntiAlias = true }
            // 绘制背景
            drawRRect(
                RRect.makeXYWH(
                    l = 0F,
                    t = 0F,
                    w = surface.width.toFloat(),
                    h = surface.height.toFloat(),
                    radius = 10F
                ),
                paint.apply { color = bgColor }
            )
            // 绘制文字
            for ((index, line) in lines.withIndex()) {
                var x = paddingX
                val y = paddingY + font.size * (lineHeight - 1) / 2 + font.size * lineHeight * (index + 0.5F)
                val keyStart = line.indexOf("：")
                var keyEnd = line.indexOf("，")
                if (keyEnd == -1) keyEnd = line.length
                if (keyStart == -1) {
                    drawString(
                        line,
                        x,
                        y,
                        font,
                        paint.apply { color = textColor }
                    )
                } else {
                    drawString(
                        line.substring(0, keyStart + 1),
                        x,
                        y,
                        font,
                        paint.apply { color = textColor }
                    )
                    x += font.measureText(line.substring(0, keyStart + 1)).width + fontSize / 2
                    drawString(
                        line.substring(keyStart + 1, keyEnd),
                        x,
                        y,
                        font,
                        paint.apply { color = keywordColor }
                    )
                    x += font.measureText(line.substring(keyStart + 1, keyEnd)).width
                    drawString(
                        line.substring(keyEnd),
                        x,
                        y,
                        font,
                        paint.apply { color = textColor }
                    )
                }
            }
        }
        return SkiaExternalResource(surface.makeImageSnapshot(), EncodedImageFormat.PNG)
    }

}
