package org.hff.miraiomnitrix.command.group

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.angles.Degrees
import com.sksamuel.scrimage.canvas.Canvas
import com.sksamuel.scrimage.canvas.GraphicsContext
import com.sksamuel.scrimage.canvas.drawables.Text
import com.sksamuel.scrimage.color.Grayscale
import com.sksamuel.scrimage.format.FormatDetector
import com.sksamuel.scrimage.nio.AnimatedGifReader
import com.sksamuel.scrimage.nio.ImageSource
import com.sksamuel.scrimage.nio.StreamingGifWriter
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.GroupCommand
import org.hff.miraiomnitrix.utils.*
import org.hff.miraiomnitrix.utils.ImageUtil.toImmutableImage
import org.hff.miraiomnitrix.utils.ImageUtil.toOval
import org.hff.miraiomnitrix.utils.ImageUtil.toStream
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.time.Duration
import kotlin.math.roundToInt

interface MemeCommand : GroupCommand {
    override val needHead: Boolean
        get() = false
}

suspend inline fun GroupMessageEvent.handleImageOrGif(
    inputStream: InputStream,
    handleImage: ImmutableImage.() -> ImmutableImage,
): Image {
    val detect = FormatDetector.detect(inputStream)
    inputStream.reset()
    if (!detect.isPresent || detect.get().ordinal != 1) {
        inputStream.toImmutableImage().handleImage().toStream().let { return uploadImage(it) }
    }

    val gif = AnimatedGifReader.read(ImageSource.of(inputStream))
    ByteArrayOutputStream().use {
        StreamingGifWriter().withCompression(true).prepareStream(it, BufferedImage.TYPE_INT_ARGB).use { newGif ->
            gif.frames.withIndex().forEach { (index, frame) ->
                newGif.writeFrame(frame.handleImage(), gif.getDelay(index))
            }
        }
        return uploadImage(it.toByteArray())
    }
}

private suspend fun GroupMessageEvent.handleGifBatch(
    duration: Long,
    frames: List<ImmutableImage>,
    block: ImmutableImage.() -> ImmutableImage
): Image {
    ByteArrayOutputStream().use {
        StreamingGifWriter(Duration.ofMillis(duration), true, false).prepareStream(it, BufferedImage.TYPE_INT_ARGB)
            .use { gif ->
                frames.forEach { frame ->
                    val newFrame = frame.block()
                    gif.writeFrame(newFrame)
                }
            }
        return uploadImage(it.toByteArray())
    }
}

@Command(name = ["addiction", "dyfz", "毒瘾发作"])
class Addiction : MemeCommand {
    private val basic = ImageUtil.load("/img/other/addiction.png", 200, 200)

    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        getImageOrAt(args)?.let {
            handleImageOrGif(it) { basic.overlay(scaleTo(60, 60), 10, 10) }
        }
}

@Command(name = ["一直", "always"])
class Always : MemeCommand {
    private val basic = ImageUtil.load("/img/other/always.png", 400, 500)
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(getImageOrAtOrSender(args)) {
            basic.overlay(scaleTo(400, 400))
                .overlay(scaleTo(60, 60), 230, 420)
        }
}

@Command(name = ["一直一直"])
class AlwaysAlways : MemeCommand {
    private val basic = ImageUtil.load("/img/other/always.png", 400, 500)
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val bigAvatar = getImageOrAtOrSender(args).toImmutableImage(400, 400)
        val image = basic.overlay(bigAvatar)
        ByteArrayOutputStream().use {
            StreamingGifWriter().prepareStream(it, BufferedImage.TYPE_INT_ARGB).use { gif ->
                val bottom1 = image.copy().scaleTo(64, 80)
                val bottom2 = bottom1.copy().scaleTo(10, 13)
                val bottom3 = image.overlay(bottom1, 228, 410).overlay(bottom2, 264, 477)
                gif.writeFrame(bottom3, Duration.ofMillis(100))
                for (i in 6..25) {
                    val width = 64 + (6..i).reduce { a, b -> a + b }
                    val delX = 400 * (1 - 64.0 / width)
                    val right = 400 - (19 * delX / 28)
                    val left = (right - 9 * delX / 28)

                    val height = (width * 1.25)
                    val delY = 500 * (1 - 80.0 / height)
                    val bottom = 500 - (41 * delY / 42)
                    val top = (bottom - 1 * delY / 42)

                    val scale1 = 400.0 / left
                    val scale2 = 500.0 / top
                    val marginLeft = (right - 172) * scale1
                    val marginTop = (bottom - 90) * scale2
                    val newBottom = bottom3.copy().scaleTo(width, height.roundToInt())
                    val newImage = image
                        .takeRight(right.roundToInt())
                        .takeLeft(left.roundToInt())
                        .takeBottom(bottom.roundToInt())
                        .takeTop(top.roundToInt())
                        .scaleTo(400, 500).overlay(newBottom, marginLeft.roundToInt(), marginTop.roundToInt())
                    gif.writeFrame(newImage, Duration.ofMillis(100))
                }
            }
            return uploadImage(it.toByteArray())
        }
    }
}

@Command(name = ["anti", "不可以", "网友见面"])
class Anti : MemeCommand {
    private val basic = ImageUtil.load("/img/other/anti_kidnap.png")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val empty = basic.empty().toImmutableImage()
        return handleImageOrGif(getImageOrAtOrSender(args)) {
            empty.overlay(scaleTo(450, 450).toOval(), 20, 80).overlay(basic)
        }
    }
}

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

@Command(name = ["蹭"])
class CapooRub : MemeCommand {
    private val frames = ImageUtil.loadBatch("/img/other/capoo_rub_%s.png", 4)
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val empty = ImmutableImage.create(512, 512)
        val image = getImageOrAt(args)?.toImmutableImage(180, 180) ?: return null
        return handleGifBatch(100, frames) {
            empty.overlay(image, 80, 260).overlay(this)
        }
    }
}

@Command(name = ["迷惑"])
class Confuse : MemeCommand {
    private val frames = ImageUtil.loadBatch("/img/other/confuse/%s.png", 100)
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val image = getImageOrAt(args)?.toImmutableImage()?.bound(400, 400) ?:return null
        return handleGifBatch(50, frames) {
            image.overlay(scaleTo(image.width, image.height))
        }
    }
}

@Command(name = ["胡桃啃"])
class HutaoBite : MemeCommand {
    private val frames = ImageUtil.loadBatch("/img/other/hutao_bite/%s.png", 2)
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val empty = ImmutableImage.create(328, 388)
        val image = getImageOrAtOrSender(args).toImmutableImage(100,100)
        return handleGifBatch(90, frames) {
            empty.overlay(image,105,235).overlay(this)
        }
    }
}

@Command(name = ["china"])
class China : MemeCommand {
    private val basic = ImageUtil.load("/img/other/china_flag.png")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(getImageOrAtOrSender(args)) {
            scaleTo(640, 640).overlay(basic)
        }

}

@Command(name = ["coupon", "陪睡券"])
class Coupon : MemeCommand {
    private val basic = ImageUtil.load("/img/other/coupon.png")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val (qq, newArgs) = args.getQqAndRemove()
        if (qq == null) return null
        val member = group[qq] ?: return null
        val first = member.nameCardOrNick + "的" + newArgs.getOrElse(0) { "陪睡" } + "券"
        val second = newArgs.getOrElse(1) { "(永久有效)" }
        val context = GraphicsContext {
            it.color = Color.BLACK
            it.font = Font("SimSun", Font.BOLD, 38)
            it.rotate(-0.2)
        }
        Canvas(basic).draw(Text(first, 0, 250, context)).draw(Text(second, 25, 300, context))
            .image.toStream().let { return uploadImage(it) }
    }
}

@Command(name = ["恐龙"])
class Dinosaur : MemeCommand {
    private val basic = ImageUtil.load("/img/other/dinosaur.png")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val empty = ImmutableImage.create(1080, 1080)
        return handleImageOrGif(getImageOrAtOrSender(args)) {
            empty.overlay(scaleTo(700, 700).toOval(), 275, 350).overlay(basic)
        }
    }
}

@Command(name = ["离婚"])
class Divorce : MemeCommand {
    private val basic = ImageUtil.load("/img/other/divorce.png")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val empty = ImmutableImage.create(1080, 1448)
        return handleImageOrGif(getImageOrAtOrSender(args)) {
            empty.overlay(scaleTo(1080, 1448)).overlay(basic)
        }
    }
}

@Command(name = ["不要靠近"])
class DontTouch : MemeCommand {
    private val basic = ImageUtil.load("/img/other/dont_touch.png")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(getImageOrAtOrSender(args)) {
            basic.overlay(scaleTo(100, 100), 100, 250)
        }

}

@Command(name = ["flip", "翻转"])
class Flip : MemeCommand {
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        getImageOrAt(args)?.let { handleImageOrGif(it) { flipX() } }
}

@Command(name = ["枪", "gun"])
class Gun : MemeCommand {
    private val basic = ImageUtil.load("/img/other/gun.png")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val empty = basic.empty().toImmutableImage()
        return handleImageOrGif(getImageOrAtOrSender(args)) {
            empty.overlay(scaleTo(500, 500)).overlay(basic)
        }
    }
}

@Command(name = ["共进晚餐", "共进午餐"])
class HaveLunch : MemeCommand {
    private val basic = ImageUtil.load("/img/other/have_lunch.jpg")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        getImageOrAt(args)?.let {
            handleImageOrGif(it) {
                basic.overlay(scaleTo(320, 320).autocrop(color(1, 1).toAWT()), 550, 35)
            }
        }

}

@Command(name = ["cheems"])
class HoldTight : MemeCommand {
    private val basic = ImageUtil.load("/img/other/hold_tight.png")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val empty = basic.empty().toImmutableImage()
        return handleImageOrGif(getImageOrAtOrSender(args)) {
            empty.overlay(scaleTo(170, 170), 110, 205).overlay(basic)
        }
    }
}

@Command(name = ["急急国王", "jjgw"])
class Jjgw : MemeCommand {
    private val basic = ImageUtil.load("/img/other/jjgw.jpg", 400, 400)
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(getImageOrAtOrSender(args)) {
            basic.overlay(scaleTo(100, 120), 190, 5)
        }

}

@Command(name = ["loading"])
class Loading : MemeCommand {
    private val basic = ImageUtil.load("/img/other/loading_0.png")
    private val icon = ImageUtil.load("/img/other/loading_1.png")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val avatar1 = getImageOrAtOrSender(args).toImmutableImage(300, 300).toOval()
        val avatar2 = avatar1.copy().scaleTo(50, 50)
        basic.overlay(avatar1.brightness(0.75)).overlay(avatar2, 60, 310).overlay(icon, 100, 100)
            .toStream().let { return uploadImage(it) }
    }
}

@Command(name = ["永远爱你","love you"])
class LoveYou : MemeCommand {
    private val basic1 = ImageUtil.load("/img/other/love_you_0.png")
    private val basic2 = ImageUtil.load("/img/other/love_you_1.png")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val avatar2 = getImageOrAt(args)?.toImmutableImage(80, 80) ?:return null
        val avatar1 = avatar2.copy().scaleTo(70, 70)
        ByteArrayOutputStream().use {
            StreamingGifWriter().withFrameDelay(Duration.ofMillis(150)).prepareStream(it, BufferedImage.TYPE_INT_ARGB)
                .use { gif ->
                    val empty = ImmutableImage.filled(202, 205, Color.WHITE)
                    val frame1 = empty.toImmutableImage().overlay(avatar1,68,65).overlay(basic1)
                    val frame2 = empty.toImmutableImage().overlay(avatar2,63,58).overlay(basic2)
                    gif.writeFrame(frame1).writeFrame(frame2)
                }
            return uploadImage(it.toByteArray())
        }
    }
}

@Command(name = ["结婚"])
class Marriage : MemeCommand {
    private val basic1 = ImageUtil.load("/img/other/marriage_0.png")
    private val basic2 = ImageUtil.load("/img/other/marriage_1.png")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        getImageOrAt(args)?.let {
            handleImageOrGif(it) {
                scaleTo(1080, 1080).overlay(basic1).overlay(basic2, 800, 0)
            }
        }
}

@Command(name = ["老婆"])
class MyWaifu : MemeCommand {
    private val basic = ImageUtil.load("/img/other/my_waifu.png")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val image = getImageOrAt(args)?.toImmutableImage(410, 410) ?: return null
        basic.overlay(image, 7, 72).toStream().let { return uploadImage(it) }
    }
}

@Command(name = ["未响应", "没有响应"])
class NoResponse : MemeCommand {
    private val basic = ImageUtil.load("/img/other/no_response.png")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val image = getImageOrAtOrSender(args).toImmutableImage(1050, 1050).brightness(0.75)
        basic.overlay(image, 0, 314).toStream().let { return uploadImage(it) }
    }
}

@Command(name = ["小画家"])
class Painter : MemeCommand {
    private val basic = ImageUtil.load("/img/other/painter.png")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val empty = basic.empty().toImmutableImage()
        return getImageOrAt(args)?.let {
            handleImageOrGif(it) {
                empty.overlay(scaleTo(250, 350), 120, 90).overlay(basic)
            }
        }
    }
}

@Command(name = ["prpr", "舔屏"])
class Prpr : MemeCommand {
    private val basic = ImageUtil.load("/img/other/prpr.png")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val empty = basic.empty().toImmutableImage()
        return getImageOrAt(args)?.let {
            handleImageOrGif(it) {
                empty.overlay(scaleTo(320, 340).rotate(Degrees(-10)), 20, 255).overlay(basic)
            }
        }
    }
}

@Command(name = ["smash", "砸屏"])
class Smash : MemeCommand {
    private val basic = ImageUtil.load("/img/other/smash.png")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val empty = basic.empty().toImmutableImage()
        return getImageOrAt(args)?.let {
            handleImageOrGif(it) {
                empty.overlay(scaleTo(900, 720)?.rotate(Degrees(-17)), -150, -150).overlay(basic)
            }
        }
    }
}

@Command(name = ["support", "精神支柱"])
class Support : MemeCommand {
    private val basic = ImageUtil.load("/img/other/support.png")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val empty = basic.empty().toImmutableImage()
        return getImageOrAt(args)?.let {
            handleImageOrGif(it) {
                val image = scaleTo(820, 820).rotate(Degrees(-23))
                empty.overlay(image, -172, -20).overlay(basic)
            }
        }
    }
}

@Command(name = ["想什么"])
class ThinkWhat : MemeCommand {
    private val basic = ImageUtil.load("/img/other/think_what.png")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val empty = basic.empty().toImmutableImage()
        return getImageOrAt(args)?.let {
            handleImageOrGif(it) {
                empty.overlay(scaleTo(520, 520), 540, 0).overlay(basic)
            }
        }
    }
}

@Command(name = ["胡桃平板"])
class WalnutPad : MemeCommand {
    private val basic = ImageUtil.load("/img/other/walnut_pad.png")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val empty = basic.empty().toImmutableImage()
        return getImageOrAt(args)?.let {
            handleImageOrGif(it) {
                empty.overlay(scaleTo(540, 360), 370, 65).overlay(basic)
            }
        }
    }
}

@Command(name = ["work", "回到工作"])
class Work : MemeCommand {
    private val basic = ImageUtil.load("/img/other/back_to_work.png")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val empty = basic.empty().toImmutableImage()
        val image = getImageOrAt(args)?.toImmutableImage(300, 300)?.rotate(Degrees(-25)) ?: return null
        empty.overlay(image, 40, 20).overlay(basic).toStream().let { return uploadImage(it) }
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
