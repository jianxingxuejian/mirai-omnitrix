package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.utils.ImageUtil
import org.hff.miraiomnitrix.utils.ImageUtil.toStream
import org.hff.miraiomnitrix.utils.uploadImage
import java.awt.Color
import java.awt.Font

@Command(name = ["xb", "喜报", "xibao"])
class Xibao : AnyCommand {

    private val xibao = ImageUtil.load("/img/other/xibao.png")

    override suspend fun MessageEvent.execute(args: List<String>): Message? {
        if (args.isEmpty()) return "请输入文字".toPlainText()
        val lines = args.joinToString(" ")
        val font = Font("simhei", Font.BOLD, 55)
        val color = Color(219, 49, 33)
        return ImageUtil.drawTextLines(xibao, lines, 120, 200, color, font).image.toStream().let { uploadImage(it) }
    }

}
