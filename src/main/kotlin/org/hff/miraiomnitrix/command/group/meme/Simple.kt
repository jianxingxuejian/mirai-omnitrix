package org.hff.miraiomnitrix.command.group.meme

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.angles.Degrees
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Message
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.utils.ImageUtil
import org.hff.miraiomnitrix.utils.ImageUtil.drawText
import org.hff.miraiomnitrix.utils.ImageUtil.perspectiveTransform
import java.awt.Color
import java.awt.Font
import kotlin.math.roundToInt

@Command(name = ["二次元入口"])
class AcgEntrance : MemeCommand {
    private val basic = ImageUtil.load("/img/other/acg_entrance.png")
    private val empty = basic.empty().toImmutableImage()
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(args) {
            basic.overlay(scaleTo(60, 60), 10, 10)
        }
}

@Command(name = ["addiction", "dyfz", "毒瘾发作"])
class Addiction : MemeCommand {
    private val basic = ImageUtil.load("/img/other/addiction.png", 200, 200)
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(args) {
            basic.overlay(scaleTo(60, 60), 10, 10)
        }
}

@Command(name = ["一直", "always"])
class Always : MemeCommand {
    private val basic = ImageUtil.load("/img/other/always.png").takeBottom(70)
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(args, isSender = true) {
            val bigImage = scaleToWidth(300)
            val empty = ImmutableImage.create(300, bigImage.height + 70)
            val smallImage = if (bigImage.width > bigImage.height) {
                bigImage.scaleToWidth(50)
            } else {
                bigImage.scaleToHeight(50)
            }
            with(smallImage) {
                val (x, y) = if (width > height) {
                    170 to bigImage.height + 10 + ((width - height) / 2.0).roundToInt()
                } else {
                    170 + ((height - width) / 2.0).roundToInt() to bigImage.height + 10
                }
                empty.overlay(bigImage).overlay(basic, 0, bigImage.height).overlay(smallImage, x, y)
            }
        }
}

@Command(name = ["anti"])
class Anti : MemeCommand {
    private val basic = ImageUtil.load("/img/other/anti_kidnap.png")
    private val empty = basic.empty().toImmutableImage()
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(args, isSender = true) {
            empty.overlay(scaleTo(450, 450), 20, 80).overlay(basic)
        }
}

@Command(name = ["anyasuki", "阿尼亚想要", "阿尼亚喜欢这个"])
class AnyaSuki : MemeCommand {
    private val basic = ImageUtil.load("/img/other/anyasuki.png")
        .drawText("阿尼亚喜欢这个", 100, 470) {
            it.font = Font("SimSun", Font.BOLD, 40)
            it.color = Color.WHITE
        }
    private val empty = basic.empty().toImmutableImage()
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(args) {
            empty.overlay(scaleTo(305, 235), 106, 72).overlay(basic)
        }
}

@Command(name = ["work", "回到工作"])
class BackToWork : MemeCommand {
    private val basic = ImageUtil.load("/img/other/back_to_work.png")
    private val empty = basic.empty().toImmutableImage()
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(args) {
            val image = scaleTo(300, 300).rotate(Degrees(-25))
            empty.overlay(image, 40, 20).overlay(basic)
        }
}

@Command(name = ["china"])
class China : MemeCommand {
    private val basic = ImageUtil.load("/img/other/china_flag.png")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(args, isSender = true) {
            scaleTo(640, 640).overlay(basic)
        }
}

@Command(name = ["恐龙"])
class Dinosaur : MemeCommand {
    private val basic = ImageUtil.load("/img/other/dinosaur.png")
    private val empty = ImmutableImage.create(1080, 1080)
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(args, isSender = true) {
            val image = scaleTo(700, 700)
            empty.overlay(image, 275, 350).overlay(basic)
        }

}

@Command(name = ["注意力涣散"])
class Distracted : MemeCommand {
    private val basic = ImageUtil.load("/img/other/distracted_0.png")
    private val icon = ImageUtil.load("/img/other/distracted_1.png")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(args, isSender = true) {
            val iconWidth = (width / 3.0).roundToInt()
            val iconHeight = (iconWidth * 0.4).roundToInt()
            val icon = icon.scaleTo(iconWidth, iconHeight)
            overlay(basic.scaleTo(width, height))
                .overlay(icon, iconWidth, (0.5 * height - 0.5 * iconHeight).roundToInt())
        }
}

@Command(name = ["离婚"])
class Divorce : MemeCommand {
    private val basic = ImageUtil.load("/img/other/divorce.png")
    private val empty = ImmutableImage.create(1080, 1448)
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(args) {
            empty.overlay(scaleTo(1080, 1448)).overlay(basic)
        }
}

@Command(name = ["不要靠近"])
class DontTouch : MemeCommand {
    private val basic = ImageUtil.load("/img/other/dont_touch.png")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(args) {
            basic.overlay(scaleTo(100, 100), 100, 250)
        }

}

@Command(name = ["flip", "翻转"])
class Flip : MemeCommand {
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(args) { flipX() }
}

@Command(name = ["枪", "gun"])
class Gun : MemeCommand {
    private val basic = ImageUtil.load("/img/other/gun.png")
    private val empty = basic.empty().toImmutableImage()
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(args) {
            empty.overlay(scaleTo(500, 500)).overlay(basic)
        }
}

@Command(name = ["共进晚餐", "共进午餐"])
class HaveLunch : MemeCommand {
    private val basic = ImageUtil.load("/img/other/have_lunch.jpg")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(args) {
            val image = scaleTo(320, 320).autocrop(color(1, 1).toAWT())
            basic.overlay(image, 550, 35)
        }

}

@Command(name = ["cheems"])
class HoldTight : MemeCommand {
    private val basic = ImageUtil.load("/img/other/hold_tight.png")
    private val empty = basic.empty().toImmutableImage()
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(args, isSender = true) {
            empty.overlay(scaleTo(170, 170), 110, 205).overlay(basic)
        }
}

@Command(name = ["急急国王", "jjgw"])
class Jjgw : MemeCommand {
    private val basic = ImageUtil.load("/img/other/jjgw.jpg", 400, 400)
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(args, isSender = true) {
            basic.overlay(scaleTo(100, 120), 190, 5)
        }

}

@Command(name = ["loading"])
class Loading : MemeCommand {
    private val basic = ImageUtil.load("/img/other/loading_0.png")
    private val icon = ImageUtil.load("/img/other/loading_1.png")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val inputStream = getImage(args, isSender = true)!!
        val top = ImageUtil.load(inputStream, 300, 300).brightness(0.75)
        inputStream.reset()
        return handleImageOrGif(inputStream) {
            val bottom = scaleTo(50, 50)
            basic.overlay(top).overlay(bottom, 60, 310).overlay(icon, 100, 100)
        }
    }
}

@Command(name = ["结婚"])
class Marriage : MemeCommand {
    private val basic1 = ImageUtil.load("/img/other/marriage_0.png")
    private val basic2 = ImageUtil.load("/img/other/marriage_1.png")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(args) {
            scaleTo(1080, 1080).overlay(basic1).overlay(basic2, 800, 0)
        }
}

@Command(name = ["mirror", "镜像"])
class Merge : MemeCommand {
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(args) {
            val empty = ImmutableImage.create(width * 2, height)
            empty.overlay(this).overlay(flipX(), width, 0)
        }
}

@Command(name = ["老婆"])
class MyWaifu : MemeCommand {
    private val basic = ImageUtil.load("/img/other/my_waifu.png")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(args) {
            basic.overlay(scaleTo(410, 410), 7, 72)
        }
}

@Command(name = ["未响应", "没有响应"])
class NoResponse : MemeCommand {
    private val basic = ImageUtil.load("/img/other/no_response.png")
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(args) {
            val image = scaleTo(1050, 1050).brightness(0.75)
            basic.overlay(image, 7, 72)
        }
}


@Command(name = ["小画家"])
class Painter : MemeCommand {
    private val basic = ImageUtil.load("/img/other/painter.png")
    private val empty = basic.empty().toImmutableImage()
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(args) {
            empty.overlay(scaleTo(250, 350), 120, 90).overlay(basic)
        }
}

@Command(name = ["玩游戏"])
class PlayGame : MemeCommand {
    private val basic = ImageUtil.load("/img/other/play_game.png")
    private val empty = basic.empty().toImmutableImage()
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(args) {
            val image = scaleTo(220, 160)
                .perspectiveTransform(0, 5, 227, 0, 216, 150, 0, 165)
                .rotate(Degrees(9))
            empty.overlay(image, 161, 117).overlay(basic)
        }
}

@Command(name = ["prpr", "舔屏"])
class Prpr : MemeCommand {
    private val basic = ImageUtil.load("/img/other/prpr.png")
    private val empty = basic.empty().toImmutableImage()
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(args) {
            val image = scaleTo(320, 340).rotate(Degrees(-10))
            empty.overlay(image, 20, 255).overlay(basic)
        }
}

@Command(name = ["smash", "砸屏"])
class Smash : MemeCommand {
    private val basic = ImageUtil.load("/img/other/smash.png")
    private val empty = basic.empty().toImmutableImage()
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(args) {
            val image = scaleTo(900, 720)?.rotate(Degrees(-17))
            empty.overlay(image, -150, -150).overlay(basic)
        }
}

@Command(name = ["support", "精神支柱"])
class Support : MemeCommand {
    private val basic = ImageUtil.load("/img/other/support.png")
    private val empty = basic.empty().toImmutableImage()
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(args) {
            val image = scaleTo(820, 820).rotate(Degrees(-23))
            empty.overlay(image, -172, -20).overlay(basic)
        }
}

@Command(name = ["想什么"])
class ThinkWhat : MemeCommand {
    private val basic = ImageUtil.load("/img/other/think_what.png")
    private val empty = basic.empty().toImmutableImage()
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(args) {
            empty.overlay(scaleTo(520, 520), 540, 0).overlay(basic)
        }
}

@Command(name = ["胡桃平板"])
class WalnutPad : MemeCommand {
    private val basic = ImageUtil.load("/img/other/walnut_pad.png")
    private val empty = basic.empty().toImmutableImage()
    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? =
        handleImageOrGif(args) {
            empty.overlay(scaleTo(540, 360), 370, 65).overlay(basic)
        }
}
