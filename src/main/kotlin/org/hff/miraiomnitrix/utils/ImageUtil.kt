package org.hff.miraiomnitrix.utils

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.canvas.Canvas
import com.sksamuel.scrimage.canvas.GraphicsContext
import com.sksamuel.scrimage.canvas.drawables.Text
import com.sksamuel.scrimage.nio.ImageWriter
import com.sksamuel.scrimage.nio.PngWriter
import org.springframework.core.io.ClassPathResource
import java.awt.Color
import java.awt.Font
import java.io.ByteArrayInputStream
import java.io.InputStream

object ImageUtil {

    private val loader = ImmutableImage.loader()

    private fun load(bytes: ByteArray): ImmutableImage = loader.fromBytes(bytes)

    fun load(path: String) = load(ClassPathResource(path).inputStream.use { it.readBytes() })

    fun load(path: String, width: Int, height: Int): ImmutableImage = load(path).scaleTo(width, height)

    fun scaleTo(inputStream: InputStream, width: Int, height: Int): ImmutableImage =
        inputStream.use { loader.fromStream(it).scaleTo(width, height) }

    fun ImmutableImage.toStream() = toStream(PngWriter())

    fun ImmutableImage.toStream(write: ImageWriter) = ByteArrayInputStream(bytes(write))

    fun InputStream.toImmutableImage(width: Int, height: Int): ImmutableImage =
        use { loader.fromStream(this).scaleTo(width, height) }

    fun InputStream.toImmutableImage(): ImmutableImage = use { loader.fromStream(this) }

    fun drawTextLines(
        bottomImage: ImmutableImage,
        lines: String,
        paddingLeft: Int,
        paddingTop: Int,
        color: Color,
        font: Font,
        lineHeight: Double = 1.2,
    ): Canvas {
        val width = bottomImage.width
        val lineLimit = (width - 2 * paddingLeft) / font.size
        val count = lines.length / lineLimit
        val context = GraphicsContext {
            it.color = color
            it.font = font
        }
        val canvas = Canvas(bottomImage.copy())
        for (i in 0..count) {
            val start = i * lineLimit
            var end = (i + 1) * lineLimit - 1
            if (end >= lines.length) end = lines.length - 1
            val line = lines.slice(start..end)
            val text = Text(
                line,
                (width - line.length * font.size) / 2,
                paddingTop + (i * font.size * lineHeight).toInt(),
                context
            )
            canvas.drawInPlace(text)
        }
        return canvas
    }

}
