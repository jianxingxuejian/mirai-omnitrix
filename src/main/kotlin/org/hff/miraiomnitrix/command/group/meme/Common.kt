package org.hff.miraiomnitrix.command.group.meme


import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.format.FormatDetector
import com.sksamuel.scrimage.nio.AnimatedGifReader
import com.sksamuel.scrimage.nio.ImageSource
import com.sksamuel.scrimage.nio.StreamingGifWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import org.hff.miraiomnitrix.command.GroupCommand
import org.hff.miraiomnitrix.common.getImage
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.ImageUtil.toImmutableImage
import org.hff.miraiomnitrix.utils.ImageUtil.toStream
import org.hff.miraiomnitrix.utils.download
import org.hff.miraiomnitrix.utils.getQq
import org.hff.miraiomnitrix.utils.uploadImage
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.time.Duration

/** 表情包指令，不需要指令头 */
sealed interface MemeCommand : GroupCommand {
    override val needHead: Boolean
        get() = false
}

/**
 * 获取图片，然后根据是否为gif分别处理图片
 *
 * @param args 追加参数
 * @param isAt 是否获取追加参数中的qq号对应的头像
 * @param isSender 是否获取发送者的qq头像
 * @param handle 处理函数
 * @return 处理后的图片
 */
suspend inline fun GroupMessageEvent.handleImageOrGif(
    args: List<String>,
    isAt: Boolean = true,
    isSender: Boolean = false,
    crossinline handle: ImmutableImage.() -> ImmutableImage
): Image? {
    val inputStream = getImage(args, isAt, isSender) ?: return null
    return handleImageOrGif(inputStream, handle)
}

/** @see handleImageOrGif */
suspend inline fun GroupMessageEvent.handleImageOrGif(
    inputStream: InputStream,
    crossinline handle: ImmutableImage.() -> ImmutableImage
): Image {
    val detect = FormatDetector.detect(inputStream)
    withContext(Dispatchers.IO) { inputStream.reset() }

    if (!detect.isPresent || detect.get().ordinal != 1) {
        inputStream.toImmutableImage().handle().toStream().let { return uploadImage(it) }
    }

    inputStream.use {
        val gif = AnimatedGifReader.read(ImageSource.of(it))
        return generateGif {
            gif.frames.withIndex().forEach { (index, frame) ->
                writeFrame(frame.handle(), gif.getDelay(index))
            }
        }
    }
}

fun InputStream.isGif(): Boolean {
    val detect = FormatDetector.detect(this)
    reset()
    return detect.isPresent && detect.get().ordinal == 1
}


/**
 * 获取图片
 *
 * @param args 追加参数
 * @param isAt 是否获取追加参数中的qq号对应的头像
 * @param isSender 是否获取发送者的qq头像
 */
suspend fun GroupMessageEvent.getImage(
    args: List<String>,
    isAt: Boolean = true,
    isSender: Boolean = false
): InputStream? =
    when (val image = message.getImage()) {
        null -> {
            var qq: Long? = null
            if (args.firstOrNull() == "自己") {
                qq = sender.id
            } else {
                if (isAt) qq = args.getQq()
                if ((qq == null) && isSender) qq = sender.id
            }
            qq?.download()
        }

        else -> HttpUtil.getInputStream(image.queryUrl())
    }

/**
 * 生成新的gif
 *
 * @param duration gif的帧延迟
 * @param handle 处理函数
 * @return 新的gif
 */
suspend fun GroupMessageEvent.generateGif(
    duration: Long = 100,
    handle: StreamingGifWriter.GifStream.() -> Unit
): Image = ByteArrayOutputStream().use {
    StreamingGifWriter(Duration.ofMillis(duration), true, false)
        .prepareStream(it, BufferedImage.TYPE_INT_ARGB)
        .use { gif -> gif.handle() }
    uploadImage(it.toByteArray())
}

/**
 * 批量处理每一帧，生成新的gif
 *
 * @param duration gif的帧延迟
 * @param frames 需要处理的所有帧
 * @param handle 处理函数
 * @return 新的gif
 */
suspend fun GroupMessageEvent.handleGifBatch(
    duration: Long = 100,
    frames: List<ImmutableImage>,
    handle: ImmutableImage.() -> ImmutableImage
): Image = generateGif(duration) {
    frames.forEach { frame ->
        val newFrame = frame.handle()
        writeFrame(newFrame)
    }
}

data class Point(
    val x1: Int, val y1: Int,
    val x2: Int, val y2: Int,
    val x3: Int, val y3: Int,
    val x4: Int, val y4: Int,
    val x: Int, val y: Int,
)

data class Rect(
    val w: Int, val h: Int,
    val x: Int, val y: Int,
)
