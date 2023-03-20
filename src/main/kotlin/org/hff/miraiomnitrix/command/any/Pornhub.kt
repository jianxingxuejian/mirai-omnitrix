package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.utils.FontFamily
import org.hff.miraiomnitrix.utils.SkiaExternalResource
import org.hff.miraiomnitrix.utils.SkikoUtil
import org.jetbrains.skia.*

@Command(name = ["pornhub", "ph"])
class Pornhub : AnyCommand {

    override suspend fun execute(args: List<String>, event: MessageEvent): Message? {
        if (args.size == 1) return "参数错误".toPlainText()
        val first = args.getOrElse(0) { "Porn" }
        val second = args.getOrElse(1) { "hub" }
        val fontFamily = if (args.size >= 3) args.drop(2).joinToString(" ") else null
        draw(first, second, fontFamily).use { return event.subject.uploadImage(it) }
    }

    fun draw(porn: String = "Porn", hub: String = "Hub", fontFamily: String?): SkiaExternalResource {
        val font = SkikoUtil.getFont(fontFamily ?: FontFamily.Tahoma.value, FontStyle.BOLD, 90F)
        val prefix = TextLine.make(porn, font)
        val suffix = TextLine.make(hub, font)
        val black = Paint().setARGB(0xFF, 0x00, 0x00, 0x00)
        val white = Paint().setARGB(0xFF, 0xFF, 0xFF, 0xFF)
        val yellow = Paint().setARGB(0xFF, 0xFF, 0x90, 0x00)

        val surface =
            Surface.makeRasterN32Premul((prefix.width + suffix.width + 50).toInt(), (suffix.height + 40).toInt())
        with(surface.canvas) {
            clear(black.color)
            drawTextLine(prefix, 10F, 20 - font.metrics.ascent, white)
            drawRRect(RRect.makeXYWH(prefix.width + 15, 15F, suffix.width + 20, suffix.height + 10, 10F), yellow)
            drawTextLine(suffix, prefix.width + 25, 20 - font.metrics.ascent, black)
        }
        return SkiaExternalResource(surface.makeImageSnapshot(), EncodedImageFormat.PNG)
    }
}
