package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.utils.FontFamily
import org.hff.miraiomnitrix.utils.SkiaExternalResource
import org.hff.miraiomnitrix.utils.SkikoUtil
import org.hff.miraiomnitrix.utils.toResource
import org.jetbrains.skia.*

@Command(name = ["红白", "choyen", "5000"])
class Choyen : AnyCommand {

    override suspend fun MessageEvent.execute(args: List<String>): Message? {
        if (args.size < 2) return "参数错误".toPlainText()
        draw(args[0], args[1]).use { return subject.uploadImage(it) }
    }

    fun draw(top: String, bottom: String): SkiaExternalResource {
        val sans = SkikoUtil.getFont(FontFamily.NotoSansSC.value, FontStyle.BOLD, 100F)
        val serif = SkikoUtil.getFont(FontFamily.NotoSerifSC.value, FontStyle.BOLD, 100F)
        val red = TextLine.make(top, sans)
        val silver = TextLine.make(bottom, serif)
        val width = maxOf(red.textBlob!!.blockBounds.right + 70, silver.textBlob!!.blockBounds.right + 250).toInt()
        val surface = Surface.makeRasterN32Premul(width, 290)
        surface.canvas.skew(-0.45F, 0F)
        with(surface.canvas) {
            val x = 70F
            val y = 100F
            val paint = Paint().setStroke(true)

            paint.strokeCap = PaintStrokeCap.ROUND
            paint.strokeJoin = PaintStrokeJoin.ROUND
            // 黒 22
            drawTextLine(red, x + 4, y + 4, paint.apply {
                shader = null
                color = Color.makeRGB(0, 0, 0)
                strokeWidth = 22F
            })
            // 銀 20
            drawTextLine(red, x + 4, y + 4, paint.apply {
                shader = Shader.makeLinearGradient(
                    0F, 24F, 0F, 122F, intArrayOf(
                        Color.makeRGB(0, 15, 36),
                        Color.makeRGB(255, 255, 255),
                        Color.makeRGB(55, 58, 59),
                        Color.makeRGB(55, 58, 59),
                        Color.makeRGB(200, 200, 200),
                        Color.makeRGB(55, 58, 59),
                        Color.makeRGB(25, 20, 31),
                        Color.makeRGB(240, 240, 240),
                        Color.makeRGB(166, 175, 194),
                        Color.makeRGB(50, 50, 50)
                    ), floatArrayOf(0.0F, 0.10F, 0.18F, 0.25F, 0.5F, 0.75F, 0.85F, 0.91F, 0.95F, 1F)
                )
                strokeWidth = 20F
            })
            // 黒 16
            drawTextLine(red, x, y, paint.apply {
                shader = null
                color = Color.makeRGB(0, 0, 0)
                strokeWidth = 16F
            })
            // 金 10
            drawTextLine(red, x, y, paint.apply {
                shader = Shader.makeLinearGradient(
                    0F, 20F, 0F, 100F, intArrayOf(
                        Color.makeRGB(253, 241, 0),
                        Color.makeRGB(245, 253, 187),
                        Color.makeRGB(255, 255, 255),
                        Color.makeRGB(253, 219, 9),
                        Color.makeRGB(127, 53, 0),
                        Color.makeRGB(243, 196, 11),
                    ), floatArrayOf(0.0F, 0.25F, 0.4F, 0.75F, 0.9F, 1F)
                )
                strokeWidth = 10F
            })
            // 黒 6
            drawTextLine(red, x + 2, y - 3, paint.apply {
                shader = null
                color = Color.makeRGB(0, 0, 0)
                strokeWidth = 6F
            })
            // 白 6
            drawTextLine(red, x, y - 3, paint.apply {
                shader = null
                color = Color.makeRGB(255, 255, 255)
                strokeWidth = 6F
            })
            // 赤 4
            drawTextLine(red, x, y - 3, paint.apply {
                shader = Shader.makeLinearGradient(
                    0F, 20F, 0F, 100F, intArrayOf(
                        Color.makeRGB(255, 100, 0),
                        Color.makeRGB(123, 0, 0),
                        Color.makeRGB(240, 0, 0),
                        Color.makeRGB(5, 0, 0),
                    ), floatArrayOf(0.0F, 0.5F, 0.51F, 1F)
                )
                strokeWidth = 4F
            })
            // 赤
            drawTextLine(red, x, y - 3, paint.setStroke(false).apply {
                shader = Shader.makeLinearGradient(
                    0F, 20F, 0F, 100F, intArrayOf(
                        Color.makeRGB(230, 0, 0),
                        Color.makeRGB(123, 0, 0),
                        Color.makeRGB(240, 0, 0),
                        Color.makeRGB(5, 0, 0),
                    ), floatArrayOf(0.0F, 0.5F, 0.51F, 1F)
                )
            })
        }
        with(surface.canvas) {
            val x = 250F
            val y = 230F
            val paint = Paint().setStroke(true)

            paint.strokeCap = PaintStrokeCap.ROUND
            paint.strokeJoin = PaintStrokeJoin.ROUND
            // 黒
            drawTextLine(silver, x + 5, y + 2, paint.apply {
                shader = null
                color = Color.makeRGB(0, 0, 0)
                strokeWidth = 22F
            })
            // 銀
            drawTextLine(silver, x + 5, y + 2, paint.apply {
                shader = Shader.makeLinearGradient(
                    0F, y - 80, 0F, y + 18, intArrayOf(
                        Color.makeRGB(0, 15, 36),
                        Color.makeRGB(250, 250, 250),
                        Color.makeRGB(150, 150, 150),
                        Color.makeRGB(55, 58, 59),
                        Color.makeRGB(25, 20, 31),
                        Color.makeRGB(240, 240, 240),
                        Color.makeRGB(166, 175, 194),
                        Color.makeRGB(50, 50, 50)
                    ), floatArrayOf(0.0F, 0.25F, 0.5F, 0.75F, 0.85F, 0.91F, 0.95F, 1F)
                )
                strokeWidth = 19F
            })
            // 黒
            drawTextLine(silver, x, y, paint.apply {
                shader = null
                color = Color.makeRGB(16, 25, 58)
                strokeWidth = 17F
            })
            // 白
            drawTextLine(silver, x, y, paint.apply {
                shader = null
                color = Color.makeRGB(221, 221, 221)
                strokeWidth = 8F
            })
            // 紺
            drawTextLine(silver, x, y, paint.apply {
                shader = Shader.makeLinearGradient(
                    0F, y - 80, 0F, y, intArrayOf(
                        Color.makeRGB(16, 25, 58),
                        Color.makeRGB(255, 255, 255),
                        Color.makeRGB(16, 25, 58),
                        Color.makeRGB(16, 25, 58),
                        Color.makeRGB(16, 25, 58),
                    ), floatArrayOf(0.0F, 0.03F, 0.08F, 0.2F, 1F)
                )
                strokeWidth = 7F
            })
            // 銀
            drawTextLine(silver, x, y - 3, paint.setStroke(false).apply {
                shader = Shader.makeLinearGradient(
                    0F, y - 80, 0F, y, intArrayOf(
                        Color.makeRGB(245, 246, 248),
                        Color.makeRGB(255, 255, 255),
                        Color.makeRGB(195, 213, 220),
                        Color.makeRGB(160, 190, 201),
                        Color.makeRGB(160, 190, 201),
                        Color.makeRGB(196, 215, 222),
                        Color.makeRGB(255, 255, 255)
                    ), floatArrayOf(0.0F, 0.15F, 0.35F, 0.5F, 0.51F, 0.52F, 1F)
                )
                strokeWidth = 19F
            })
        }
        return surface.makeImageSnapshot().toResource()
    }

}
