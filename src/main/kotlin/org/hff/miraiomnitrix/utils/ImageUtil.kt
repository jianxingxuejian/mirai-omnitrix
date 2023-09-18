package org.hff.miraiomnitrix.utils

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.canvas.Canvas
import com.sksamuel.scrimage.canvas.GraphicsContext
import com.sksamuel.scrimage.canvas.drawables.Text
import com.sksamuel.scrimage.format.FormatDetector
import com.sksamuel.scrimage.nio.ImageWriter
import com.sksamuel.scrimage.nio.PngWriter
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.springframework.core.io.ClassPathResource
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.imageio.ImageIO

object ImageUtil {

    private val loader = ImmutableImage.loader()

    private fun load(bytes: ByteArray): ImmutableImage = loader.fromBytes(bytes)

    fun load(path: String) = load(ClassPathResource(path).inputStream.use { it.readBytes() })

    fun load(path: String, width: Int, height: Int): ImmutableImage = load(path).scaleTo(width, height)

    fun load(inputStream: InputStream): ImmutableImage = loader.fromStream(inputStream)

    fun load(inputStream: InputStream, width: Int, height: Int): ImmutableImage =
        load(inputStream).scaleTo(width, height)

    fun loadBatch(path: String, count: Int) =
        (0 until count).map { index -> load(ClassPathResource(("$path/$index.png")).inputStream.use { it.readBytes() }) }

    fun scaleTo(inputStream: InputStream, width: Int, height: Int): ImmutableImage =
        inputStream.use { loader.fromStream(it).scaleTo(width, height) }

    fun ImmutableImage.toStream() = toStream(PngWriter())

    fun ImmutableImage.toStream(write: ImageWriter) = ByteArrayInputStream(bytes(write))

    fun ImmutableImage.toOval(): ImmutableImage {
        val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR)
        with(bufferedImage.createGraphics()) {
            setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            clip = Ellipse2D.Double(0.0, 0.0, width.toDouble(), height.toDouble())
            drawImage(awt(), 0, 0, width, height, null)
            dispose()
        }
        return ImmutableImage.fromAwt(bufferedImage)
    }

    fun ImmutableImage.drawText(text: String, x: Int, y: Int, context: GraphicsContext): ImmutableImage =
        Canvas(this).draw(Text(text, x, y, context)).image

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

    fun ImmutableImage.perspectiveTransform(
        x1: Int, y1: Int,
        x2: Int, y2: Int,
        x3: Int, y3: Int,
        x4: Int, y4: Int,
    ): ImmutableImage {
        val data = (awt().raster.dataBuffer as DataBufferByte).data
        val src = Mat(height, width, CvType.CV_8UC3).apply { put(0, 0, data) }
        val widthDouble = width.toDouble()
        val heightDouble = height.toDouble()
        val mop1 = MatOfPoint2f(
            Point(0.0, 0.0),
            Point(widthDouble, 0.0),
            Point(widthDouble, heightDouble),
            Point(0.0, heightDouble)
        )
        val mop2 = MatOfPoint2f(
            Point(x1.toDouble(), y1.toDouble()),
            Point(x2.toDouble(), y2.toDouble()),
            Point(x3.toDouble(), y3.toDouble()),
            Point(x4.toDouble(), y4.toDouble())
        )
        val transform = Imgproc.getPerspectiveTransform(mop1, mop2)
        val dst = Mat().also { Imgproc.warpPerspective(src, it, transform, Size(widthDouble, heightDouble)) }
        val result = MatOfByte().also { Imgcodecs.imencode(".jpg", dst, it) }
        return load(result.toArray())
    }

    fun InputStream.toImmutableImage(width: Int, height: Int): ImmutableImage =
        use { loader.fromStream(this).scaleTo(width, height) }

    fun InputStream.toImmutableImage(): ImmutableImage = use { loader.fromStream(this) }

    fun ByteArray.toImmutableImage(width: Int, height: Int): ImmutableImage =
        load(this).scaleTo(width, height)

}

fun InputStream.format(formatName: String): ByteArray = use {
    val detect = FormatDetector.detect(it)
    reset()
    if (!detect.isPresent) return readBytes()
    val format = detect.get()
    if (formatName == format.name) return readBytes()
    val image = ImageIO.read(it)
    val type = when (formatName) {
        "png" -> BufferedImage.TYPE_INT_ARGB
        else -> BufferedImage.TYPE_INT_RGB
    }
    val newImage = BufferedImage(image.width, image.height, type)
    newImage.createGraphics().drawImage(image, 0, 0, Color.WHITE, null)
    ByteArrayOutputStream().use { baos ->
        ImageIO.write(newImage, formatName, baos)
        baos.toByteArray()
    }
}
