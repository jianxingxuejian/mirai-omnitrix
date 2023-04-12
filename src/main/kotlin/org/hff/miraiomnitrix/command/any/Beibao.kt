package org.hff.miraiomnitrix.command.any

import com.sksamuel.scrimage.canvas.Canvas
import com.sksamuel.scrimage.canvas.drawables.Text
import com.sksamuel.scrimage.color.Grayscale
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.utils.ImageUtil
import org.hff.miraiomnitrix.utils.ImageUtil.toStream
import org.hff.miraiomnitrix.utils.download
import org.hff.miraiomnitrix.utils.getQq
import org.hff.miraiomnitrix.utils.uploadImage
import java.awt.Color
import java.awt.Font

@Command(name = ["bb", "悲报", "beibao"])
class Beibao : AnyCommand {

    private val beibao = ImageUtil.load("/img/other/beibao.jpg")
    private val dian = ImageUtil.load("/img/other/dian.jpg")

    override suspend fun MessageEvent.execute(args: List<String>): Message? {
        val qq = if (args.isEmpty()) sender.id else args.getQq()
        val font = Font("simhei", Font.BOLD, 55)
        val color = Color(100, 100, 100)
        return if (qq != null) {
            val avatar = qq.download().use { ImageUtil.scaleTo(it, 250, 250) }.map { pixel ->
                Grayscale(pixel.average()).toRGB().awt()
            }
            val text = Text("奠", 220, 170) {
                it.color = color
                it.font = font
            }
            Canvas(dian).draw(text).image.overlay(avatar, 117, 195).toStream().let { uploadImage(it) }
        } else {
            val lines = args.joinToString(" ")
            ImageUtil.drawTextLines(beibao, lines, 120, 200, color, font).image.toStream().let { uploadImage(it) }
        }
    }

}
