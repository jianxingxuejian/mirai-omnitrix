package org.hff.miraiomnitrix.command.group.meme

import com.sksamuel.scrimage.canvas.Canvas
import com.sksamuel.scrimage.canvas.GraphicsContext
import com.sksamuel.scrimage.canvas.drawables.Text
import com.sksamuel.scrimage.color.Grayscale
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Message
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.utils.*
import org.hff.miraiomnitrix.utils.ImageUtil.toImmutableImage
import org.hff.miraiomnitrix.utils.ImageUtil.toOval
import org.hff.miraiomnitrix.utils.ImageUtil.toStream
import java.awt.Color
import java.awt.Font

@Command(name = ["bb", "悲报", "beibao", "死"])
class Beibao : MemeCommand {
    private val beibao = ImageUtil.load("/img/other/beibao.jpg")
    private val dian = ImageUtil.load("/img/other/dian.jpg")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
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
            val lines = args.joinToString(" ").replace("_", " ")
            ImageUtil.drawTextLines(beibao, lines, 120, 200, color, font).image.toStream().let { uploadImage(it) }
        }
    }
}

@Command(name = ["coupon", "陪睡券"])
class Coupon : MemeCommand {
    private val basic = ImageUtil.load("/img/other/coupon.png")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val (qq, newArgs) = args.getQqAndRemove()
        if (qq == null) return null
        val member = group[qq] ?: return null
        val avatar = qq.download().toImmutableImage(50, 50).toOval()
        val first = member.nameCardOrNick + "的" + newArgs.getOrElse(0) { "陪睡" } + "券"
        val second = newArgs.getOrElse(1) { "(永久有效)" }
        val context = GraphicsContext {
            it.color = Color.BLACK
            it.font = Font("SimSun", Font.BOLD, 38)
            it.rotate(-0.2)
        }
        Canvas(basic.overlay(avatar, 175, 95))
            .draw(Text(first, 0, 250, context)).draw(Text(second, 25, 300, context))
            .image.toStream().let { return uploadImage(it) }
    }
}

@Command(name = ["xb", "喜报", "xibao"])
class Xibao : MemeCommand {
    private val xibao = ImageUtil.load("/img/other/xibao.png")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        if (args.isEmpty()) return null
        val lines = args.joinToString(" ")
        val font = Font("simhei", Font.BOLD, 55)
        val color = Color(219, 49, 33)
        return ImageUtil.drawTextLines(xibao, lines, 120, 200, color, font).image.toStream().let { uploadImage(it) }
    }
}
