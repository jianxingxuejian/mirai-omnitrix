package org.hff.miraiomnitrix.command.group.meme

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.AnimatedGifReader
import com.sksamuel.scrimage.nio.ImageSource
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.utils.ImageUtil
import org.hff.miraiomnitrix.utils.ImageUtil.perspectiveTransform
import org.hff.miraiomnitrix.utils.ImageUtil.toImmutableImage
import org.hff.miraiomnitrix.utils.ImageUtil.toOval
import org.hff.miraiomnitrix.utils.download
import org.hff.miraiomnitrix.utils.format
import org.hff.miraiomnitrix.utils.getQq
import java.awt.Color
import kotlin.math.roundToInt

@Command(name = ["一直一直"])
class AlwaysAlways : MemeCommand {
    private val basic = ImageUtil.load("/img/other/always.png", 400, 500)
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val bigAvatar = getImage(args, isSender = true)!!.toImmutableImage(400, 400)
        val image = basic.overlay(bigAvatar)
        return generateGif(100) {
            val bottom1 = image.copy().scaleTo(64, 80)
            val bottom2 = bottom1.copy().scaleTo(10, 13)
            val bottom3 = image.overlay(bottom1, 228, 410).overlay(bottom2, 264, 477)
            writeFrame(bottom3)
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
                image.takeRight(right.roundToInt())
                    .takeLeft(left.roundToInt())
                    .takeBottom(bottom.roundToInt())
                    .takeTop(top.roundToInt())
                    .scaleTo(400, 500).overlay(newBottom, marginLeft.roundToInt(), marginTop.roundToInt())
                    .let(::writeFrame)
            }
        }
    }
}

@Command(name = ["波奇手稿"])
class BocchiDraft : MemeCommand {
    private val frames = ImageUtil.loadBatch("/img/other/bocchi_draft", 23)
    private val empty = ImmutableImage.create(720, 405)
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val image = getImage(args, isSender = true)!!.format("jpg").toImmutableImage(350, 400)
        val idx = listOf(0, 0, 0, 0, 0, 0, 0, 1, 1, 2, 2, 2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10)
        val points = listOf(
            Point(54, 62, 353, 1, 379, 382, 1, 399, 146, 173),
            Point(54, 61, 349, 1, 379, 381, 1, 398, 146, 174),
            Point(54, 61, 349, 1, 379, 381, 1, 398, 152, 174),
            Point(54, 61, 335, 1, 379, 381, 1, 398, 158, 167),
            Point(54, 61, 335, 1, 370, 381, 1, 398, 157, 149),
            Point(41, 59, 321, 1, 357, 379, 1, 396, 167, 108),
            Point(41, 57, 315, 1, 357, 377, 1, 394, 173, 69),
            Point(41, 56, 309, 1, 353, 380, 1, 393, 175, 43),
            Point(41, 56, 314, 1, 353, 380, 1, 393, 174, 30),
            Point(41, 50, 312, 1, 348, 367, 1, 387, 171, 18),
            Point(35, 50, 306, 1, 342, 367, 1, 386, 178, 14),
        )
        return generateGif(85) {
            frames.withIndex().forEach { (index, frame) ->
                val point = points[idx[index]]
                with(point) {
                    val newImage = image.perspectiveTransform(x1, y1, x2, y2, x3, y3, x4, y4)
                    empty.overlay(newImage, x, y).overlay(frame).let(::writeFrame)
                }
            }
        }
    }
}

@Command(name = ["蹭"])
class CapooRub : MemeCommand {
    private val frames = ImageUtil.loadBatch("/img/other/capoo_rub", 4)
    private val empty = ImmutableImage.create(512, 512)
    private val rects = listOf(
        Rect(178, 184, 78, 260),
        Rect(178, 174, 84, 269),
        Rect(178, 174, 84, 269),
        Rect(178, 178, 84, 264),
    )

    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val image = getImage(args)?.toImmutableImage(200, 200) ?: return null
        return generateGif(100) {
            frames.withIndex().forEach { (index, frame) ->
                with(rects[index]) {
                    empty.overlay(image.scaleTo(w, h), x, y).overlay(frame).let(::writeFrame)
                }
            }
        }
    }
}

@Command(name = ["追火车"])
class ChaseTrain : MemeCommand {
    private val frames = ImageUtil.loadBatch("/img/other/chase_train", 120)
    private val empty = ImmutableImage.create(196, 106)
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val image = getImage(args, isSender = true)!!.toImmutableImage(40, 40)
        val rects = listOf(
            Rect(35, 34, 128, 44), Rect(35, 33, 132, 40), Rect(33, 34, 133, 36), Rect(33, 38, 135, 41),
            Rect(34, 34, 136, 38), Rect(35, 35, 136, 33), Rect(33, 34, 138, 38), Rect(36, 35, 138, 34),
            Rect(38, 34, 139, 32), Rect(40, 35, 139, 37), Rect(36, 35, 139, 33), Rect(39, 36, 138, 28),
            Rect(40, 35, 138, 33), Rect(37, 34, 138, 31), Rect(43, 36, 135, 27), Rect(36, 37, 136, 32),
            Rect(38, 40, 135, 26), Rect(37, 35, 133, 26), Rect(33, 36, 132, 30), Rect(33, 39, 132, 25),
            Rect(32, 36, 131, 23), Rect(33, 36, 130, 31), Rect(35, 39, 128, 25), Rect(33, 35, 127, 23),
            Rect(34, 36, 126, 29), Rect(34, 40, 124, 25), Rect(39, 36, 119, 23), Rect(35, 36, 119, 32),
            Rect(35, 37, 116, 27), Rect(36, 38, 113, 23), Rect(34, 35, 113, 32), Rect(39, 36, 113, 23),
            Rect(36, 35, 114, 17), Rect(36, 38, 111, 13), Rect(34, 37, 114, 15), Rect(34, 39, 111, 10),
            Rect(33, 39, 109, 11), Rect(36, 35, 104, 17), Rect(34, 36, 102, 14), Rect(34, 35, 99, 14),
            Rect(35, 38, 96, 16), Rect(35, 35, 93, 14), Rect(36, 35, 89, 15), Rect(36, 36, 86, 18),
            Rect(36, 39, 83, 14), Rect(34, 36, 81, 16), Rect(40, 41, 74, 17), Rect(38, 36, 74, 15),
            Rect(39, 35, 70, 16), Rect(33, 35, 69, 20), Rect(36, 35, 66, 17), Rect(36, 35, 62, 17),
            Rect(37, 36, 57, 21), Rect(35, 39, 57, 15), Rect(35, 36, 53, 17), Rect(35, 38, 51, 20),
            Rect(37, 36, 47, 19), Rect(37, 35, 47, 18), Rect(40, 36, 43, 19), Rect(38, 35, 42, 22),
            Rect(40, 34, 38, 20), Rect(38, 34, 37, 21), Rect(39, 32, 35, 24), Rect(39, 33, 33, 22),
            Rect(39, 36, 32, 22), Rect(38, 35, 32, 25), Rect(35, 37, 31, 22), Rect(37, 37, 31, 23),
            Rect(36, 31, 31, 28), Rect(37, 34, 32, 25), Rect(36, 37, 32, 23), Rect(36, 33, 33, 30),
            Rect(35, 34, 33, 27), Rect(38, 33, 33, 28), Rect(37, 34, 33, 29), Rect(36, 35, 35, 28),
            Rect(36, 37, 36, 27), Rect(43, 39, 33, 30), Rect(35, 34, 38, 31), Rect(37, 34, 39, 30),
            Rect(36, 34, 40, 30), Rect(39, 35, 41, 30), Rect(41, 36, 41, 29), Rect(40, 37, 44, 32),
            Rect(40, 37, 45, 29), Rect(39, 38, 48, 28), Rect(38, 33, 50, 33), Rect(35, 38, 53, 28),
            Rect(37, 34, 54, 31), Rect(38, 34, 57, 32), Rect(41, 35, 57, 29), Rect(35, 34, 63, 29),
            Rect(41, 35, 62, 29), Rect(38, 35, 66, 28), Rect(35, 33, 70, 29), Rect(40, 39, 70, 28),
            Rect(36, 36, 74, 28), Rect(37, 35, 77, 26), Rect(37, 35, 79, 28), Rect(38, 35, 81, 27),
            Rect(36, 35, 85, 27), Rect(37, 36, 88, 29), Rect(36, 34, 91, 27), Rect(38, 39, 94, 24),
            Rect(39, 34, 95, 27), Rect(37, 34, 98, 26), Rect(36, 35, 103, 24), Rect(37, 36, 99, 28),
            Rect(34, 36, 97, 34), Rect(34, 38, 102, 38), Rect(37, 37, 99, 40), Rect(39, 36, 101, 47),
            Rect(36, 36, 106, 43), Rect(35, 35, 109, 40), Rect(35, 39, 112, 43), Rect(33, 36, 116, 41),
            Rect(36, 36, 116, 39), Rect(34, 37, 121, 45), Rect(35, 41, 123, 38), Rect(34, 37, 126, 35)
        )
        return generateGif(40) {
            frames.withIndex().forEach { (index, frame) ->
                val rect = rects[index]
                with(rect) {
                    empty.overlay(image.scaleTo(w, h), x, y).overlay(frame).let(::writeFrame)
                }
            }
        }
    }
}

@Command(name = ["迷惑"])
class Confuse : MemeCommand {
    private val frames = ImageUtil.loadBatch("/img/other/confuse", 100)
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val image = getImage(args, isSender = true)?.toImmutableImage()?.bound(400, 400) ?: return null
        return handleGifBatch(50, frames) {
            image.overlay(scaleTo(image.width, image.height))
        }
    }
}

@Command(name = ["决斗", "fencing"])
class FencingGif : MemeCommand {
    private val frames = ImageUtil.loadBatch("/img/other/fencing", 19)
    val pointsLeft = listOf(
        10 to 6, 3 to 6, 32 to 7, 22 to 7, 13 to 4, 21 to 6,
        30 to 6, 22 to 2, 22 to 3, 26 to 8, 23 to 8, 27 to 10,
        30 to 9, 17 to 6, 12 to 8, 11 to 7, 8 to 6, -2 to 10, 4 to 9
    )
    val pointsRight = listOf(
        57 to 4, 55 to 5, 58 to 7, 57 to 5, 53 to 8,
        54 to 9, 64 to 5, 66 to 8, 70 to 9, 73 to 8,
        81 to 10, 77 to 10, 72 to 4, 79 to 8, 50 to 8,
        60 to 7, 67 to 6, 60 to 6, 50 to 9
    )

    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val qq = args.getQq() ?: return null
        val right = qq.download().toImmutableImage(27, 27).toOval()
        val left = sender.id.download().toImmutableImage(27, 27).toOval()
        return generateGif(80) {
            frames.withIndex().forEach { (index, frame) ->
                val (x1, y1) = pointsLeft[index]
                val (x2, y2) = pointsRight[index]
                frame.overlay(left, x1, y1).overlay(right, x2, y2).let(::writeFrame)
            }
        }
    }
}

@Command(name = ["锤", "🔨"])
class Hammer : MemeCommand {
    private val frames = ImageUtil.loadBatch("/img/other/hammer", 7)
    private val empty = ImmutableImage.create(247, 285)
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val image = getImage(args)?.toImmutableImage(200, 200) ?: return null
        val rects = listOf(
            Rect(158, 113, 62, 143),
            Rect(173, 105, 52, 177),
            Rect(192, 92, 42, 192),
            Rect(184, 100, 46, 182),
            Rect(174, 110, 54, 169),
            Rect(144, 135, 69, 128),
            Rect(152, 124, 65, 130),
        )
        return generateGif(90) {
            frames.withIndex().forEach { (index, frame) ->
                with(rects[index]) {
                    empty.overlay(image.scaleTo(w, h), x, y).overlay(frame).let(::writeFrame)
                }
            }
        }
    }
}

@Command(name = ["胡桃啃"])
class HutaoBite : MemeCommand {
    private val frames = ImageUtil.loadBatch("/img/other/hutao_bite", 2)
    private val empty = ImmutableImage.create(328, 388)
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val image = getImage(args, isSender = true)!!.toImmutableImage(100, 100)
        return handleGifBatch(90, frames) {
            empty.overlay(image, 105, 235).overlay(this)
        }
    }
}

@Command(name = ["kiss", "亲"])
class Kiss : MemeCommand {
    private val frames = ImageUtil.loadBatch("/img/other/kiss", 13)
    val pointsLeft = listOf(
        58 to 90, 62 to 95, 42 to 100, 50 to 100, 56 to 100,
        18 to 120, 28 to 110, 54 to 100, 46 to 100, 60 to 100,
        35 to 115, 20 to 120, 40 to 96
    )
    val pointsRight = listOf(
        92 to 64, 135 to 40, 84 to 105, 80 to 110, 155 to 82,
        60 to 96, 50 to 80, 98 to 55, 35 to 65, 38 to 100,
        70 to 80, 84 to 65, 75 to 65
    )

    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val qq = args.getQq() ?: return null
        val left = qq.download().toImmutableImage(50, 50).toOval()
        val right = sender.id.download().toImmutableImage(40, 40).toOval()
        return generateGif(90) {
            frames.withIndex().forEach { (index, frame) ->
                val (x1, y1) = pointsLeft[index]
                val (x2, y2) = pointsRight[index]
                frame.overlay(left, x1, y1).overlay(right, x2, y2).let(::writeFrame)
            }
        }
    }
}

@Command(name = ["knock", "敲"])
class Knock : MemeCommand {
    private val frames = ImageUtil.loadBatch("/img/other/knock", 8)
    private val empty = ImmutableImage.create(600, 532)
    private val rects = listOf(
        Rect(210, 195, 60, 308),
        Rect(210, 198, 60, 30),
        Rect(250, 172, 45, 330),
        Rect(218, 180, 58, 320),
        Rect(215, 193, 60, 310),
        Rect(250, 285, 40, 320),
        Rect(226, 192, 48, 308),
        Rect(223, 200, 51, 301),
    )

    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val image = getImage(args)?.toImmutableImage(250, 250) ?: return null
        return generateGif(50) {
            frames.withIndex().forEach { (index, frame) ->
                with(rects[index]) {
                    empty.overlay(image.scaleTo(w, h), x, y).overlay(frame).let(::writeFrame)
                }
            }
        }
    }
}

@Command(name = ["永远爱你", "love you"])
class LoveYou : MemeCommand {
    private val basic1 = ImageUtil.load("/img/other/love_you_0.png")
    private val basic2 = ImageUtil.load("/img/other/love_you_1.png")
    private val empty = ImmutableImage.filled(202, 205, Color.WHITE)
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val avatar2 = getImage(args)?.toImmutableImage(80, 80) ?: return null
        val avatar1 = avatar2.copy().scaleTo(70, 70)
        return generateGif(150) {
            val frame1 = empty.overlay(avatar1, 68, 65).overlay(basic1)
            val frame2 = empty.overlay(avatar2, 63, 58).overlay(basic2)
            writeFrame(frame1).writeFrame(frame2)
        }
    }
}

//@Command(name = ["听音乐", "听歌"])
//class ListenMusic : MemeCommand {
//    private val basic = ImageUtil.load("/img/other/listen_music.png")
//    private val empty = ImmutableImage.create(414, 399)
//    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
//        val image = getImage(args, isAt = true)?.toImmutableImage(215, 215) ?: return null
//        return generateGif(50) {
//            empty.overlay(image, 100, 100).overlay(basic).let(::writeFrame)
//            for (i in 10..350 step 10) {
//                empty.overlay(image.rotate(Degrees(i)), 100, 100).overlay(basic).let(::writeFrame)
//            }
//        }
//    }
//}

@Command(name = ["pat", "拍"])
class Pat : MemeCommand {
    private val frames = ImageUtil.loadBatch("/img/other/pat", 10)
    private val empty = ImmutableImage.create(235, 196)
    private val seq = listOf(0, 1, 2, 3, 1, 2, 3, 0, 1, 2, 3, 0, 0, 1, 2, 3, 0, 0, 0, 0, 4, 5, 5, 5, 6, 7, 8, 9)
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val image1 = getImage(args)?.toImmutableImage(106, 100) ?: return null
        val image2 = image1.scaleTo(112, 96)
        return generateGif(95) {
            seq.forEach {
                val frame = frames[it]
                if (it != 2) {
                    empty.overlay(image1, 11, 73)
                } else {
                    empty.overlay(image2, 8, 79)
                }.overlay(frame).let(::writeFrame)
            }
        }
    }
}

@Command(name = ["petpet", "摸"])
class Petpet : MemeCommand {
    private val frames = ImageUtil.loadBatch("/img/other/petpet", 5)
    private val empty = ImmutableImage.filled(112, 112, Color.WHITE)
    private val rects = listOf(
        Rect(98, 98, 14, 20),
        Rect(101, 85, 12, 33),
        Rect(110, 76, 8, 40),
        Rect(102, 84, 10, 33),
        Rect(98, 98, 12, 20),
    )

    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val image = getImage(args)?.toImmutableImage(112, 112)?.toOval() ?: return null
        return generateGif(100) {
            frames.withIndex().forEach { (index, frame) ->
                with(rects[index]) {
                    empty.overlay(image.scaleTo(w, h), x, y).overlay(frame).let(::writeFrame)
                }
            }
        }
    }
}

@Command(name = ["play", "顶", "玩"])
class Play : MemeCommand {
    private val frames = ImageUtil.loadBatch("/img/other/play", 38)
    private val rects = listOf(
        Rect(100, 100, 180, 60), Rect(100, 100, 184, 75), Rect(100, 100, 183, 98),
        Rect(110, 100, 179, 118), Rect(150, 48, 156, 194), Rect(122, 69, 178, 136),
        Rect(122, 85, 175, 66), Rect(130, 96, 170, 42), Rect(118, 95, 175, 34),
        Rect(110, 93, 179, 35), Rect(102, 93, 180, 54), Rect(97, 92, 183, 58),
        Rect(90, 96, 174, 35), Rect(120, 94, 179, 35), Rect(109, 93, 181, 54),
        Rect(101, 92, 182, 59), Rect(98, 92, 183, 71), Rect(92, 101, 180, 131)
    )
    private val empty = ImmutableImage.create(480, 400)

    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val image = getImage(args)?.toImmutableImage(130, 130) ?: return null
        val newFrames = frames.slice(0..17).mapIndexed { index, frame ->
            with(rects[index]) {
                empty.overlay(image.scaleTo(w, h), x, y).overlay(frame)
            }
        }
        return generateGif(60) {
            repeat(2) {
                newFrames.slice(0..11).forEach { writeFrame(it) }
            }
            newFrames.slice(0..7).forEach { writeFrame(it) }
            newFrames.slice(12..17).forEach { writeFrame(it) }
            frames.drop(18).forEach { writeFrame(it) }
        }
    }
}

@Command(name = ["木鱼"])
class WoodenFish : MemeCommand {
    private val frames = ImageUtil.loadBatch("/img/other/wooden_fish", 66)
    private val empty = ImmutableImage.create(480, 270)
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val image = getImage(args)?.toImmutableImage(85, 85) ?: return null
        return generateGif(60) {
            frames.forEach { frame -> empty.overlay(image, 116, 153).overlay(frame).let { writeFrame(it) } }
        }
    }
}

@Command(name = ["加速"])
class Rate : MemeCommand {
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val image = getImage(args) ?: return null
        return if (image.isGif()) {
            val rate = args.firstOrNull()?.toLongOrNull() ?: 2
            val gif = AnimatedGifReader.read(ImageSource.of(image))
            generateGif {
                gif.frames.withIndex().forEach { (index, frame) ->
                    writeFrame(frame, gif.getDelay(index).dividedBy(rate))
                }
            }
        } else "仅支持gif类型".toPlainText()
    }
}
