package org.hff.miraiomnitrix.command.group

import com.sksamuel.scrimage.canvas.Canvas
import com.sksamuel.scrimage.canvas.GraphicsContext
import com.sksamuel.scrimage.canvas.drawables.Text
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.CommandResult
import org.hff.miraiomnitrix.command.GroupCommand
import org.hff.miraiomnitrix.command.result
import org.hff.miraiomnitrix.utils.*
import org.hff.miraiomnitrix.utils.ImageUtil.toStream
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import java.awt.Color
import java.awt.Font
import java.io.ByteArrayInputStream
import java.io.File

@Command(name = ["随机老婆", "老婆", "waifu"])
class RandomWaifu : GroupCommand {

    private val genshinAvatars = hashMapOf<String, ByteArray>()

    private val bottom = ClassPathResource("/img/other/2.jpg").inputStream.readBytes()

    init {
        val files = PathMatchingResourcePatternResolver().getResources("/img/genshin/avatar/*")
        files.forEach {
            val name = it.filename?.substringBeforeLast(".")
            if (name != null) {
                genshinAvatars[name] = it.inputStream.readBytes()
            }
        }
    }

    override suspend fun execute(args: List<String>, event: GroupMessageEvent): CommandResult? {
        val (group, sender) = event.getInfo()
        val left = sender.id
        if (args.isEmpty()) {
            val member = group.members.random()
            val text = sender.nameCardOrNick + " " + member.nameCardOrNick
            val startTime = System.currentTimeMillis()
            val result = draw(sender.id, member.id, text)
            val elapsedTime = System.currentTimeMillis() - startTime
            println(elapsedTime)
            result.use { group.sendImage(it) }
        } else if (args[0] == "原神") {
            val name = genshinAvatars.keys.random()
            val right = genshinAvatars[name]!!
            draw(left, right, name).use { group.sendImage(it) }
        } else {
            val qq = Util.getQq(args)
            if (qq != null) {
                val name = group.members[qq]?.nameCardOrNick ?: return result("没有找到成员")
                draw(left, qq, name).use { group.sendImage(it) }
            } else {
                val right = genshinAvatars[args[0]] ?: return result("没有找到角色")
                draw(left, right, args[0]).use { group.sendImage(it) }
            }
        }
        return null
    }

    private fun draw(left: Long, right: Long, names: String): ByteArrayInputStream {
        val inputStreamLeft = Util.getQqImg(left)
        val inputStreamRight = Util.getQqImg(right)
        return draw(inputStreamLeft.readAllBytes(), inputStreamRight.readAllBytes(), names)
    }

    private fun draw(left: Long, right: ByteArray, names: String): ByteArrayInputStream {
        val inputStreamLeft = Util.getQqImg(left)
        return draw(inputStreamLeft.readAllBytes(), right, names)
    }

    private fun draw(left: ByteArray, right: ByteArray, names: String): ByteArrayInputStream {
        val imgLeft = ImageUtil.scaleTo(left, 160, 160)
        val context = GraphicsContext { g2 ->
            g2.color = Color.BLACK
            g2.font = Font("Tahoma", Font.PLAIN, 40)
        }
        val text = Text(names, 700, 633, context)
        val imgRight = ImageUtil.scaleTo(right, 160, 160)
        val imgBottom = ImageUtil.load(bottom)
        return Canvas(imgBottom).draw(text).image.overlay(imgLeft, 602, 100).overlay(imgRight, 768, 100).toStream()
    }

//    private fun test(left: InputStream, right: InputStream, text: String): SkiaExternalResource {
//        val leftImage = Image.makeFromEncoded(left.readBytes())
//        val rightImage = Image.makeFromEncoded(right.readBytes())
//        val surface = Surface.makeRasterN32Premul(1024, 724)
//        with(surface.canvas) {
//            val paint = Paint().apply {
//                isAntiAlias = true
//            }
//            drawImage(bottomImage, 0f, 0f, paint)
//            drawImageRect(leftImage, makeXYWH(602F, 100F, 160F, 160F), paint)
//            drawImageRect(rightImage, makeXYWH(768F, 100F, 160F, 160F), paint)
//            drawString(text, 600f, 400f, Font(Typeface.makeDefault(), 50f), paint)
//        }
//        return SkiaExternalResource(surface.makeImageSnapshot(), EncodedImageFormat.PNG)
//    }

}

fun main() {
    val json =
        HttpUtil.getString("https://api-static.mihoyo.com/common/blackboard/ys_obc/v1/home/content/list?app_sn=ys_obc&channel_id=189")
    JsonUtil.getObj(json, "data").getAsArray("list")[0].getAsArray("children")[0].getAsArray("list").forEach {
        val icon = it.getAsStr("icon")
        val img = HttpUtil.getInputStream(icon)
        val title = it.getAsStr("title")
        val filename = "src/main/resources/img/genshin/avatar/$title.png"
        val file = File(filename)
        file.outputStream().use { output -> img.copyTo(output) }
    }
}



