package org.hff.miraiomnitrix.command.group

import com.sksamuel.scrimage.canvas.Canvas
import com.sksamuel.scrimage.canvas.GraphicsContext
import com.sksamuel.scrimage.canvas.drawables.Text
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.contact.remarkOrNameCard
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.GroupCommand
import org.hff.miraiomnitrix.common.getImage
import org.hff.miraiomnitrix.utils.*
import org.hff.miraiomnitrix.utils.ImageUtil.toStream
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import java.awt.Color
import java.awt.Font
import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlin.collections.set

@Command(name = ["随机老婆", "老婆", "waifu"])
class RandomWaifu : GroupCommand {

    private val avatars = hashMapOf<String, Resource>()

    private val bottom1 = ImageUtil.load("/img/other/1.jpg")
    private val bottom2 = ImageUtil.load("/img/other/2.jpg")
    private val bottom3 = ImageUtil.load("/img/other/3.jpg")

    init {
        PathMatchingResourcePatternResolver().getResources("/img/**/avatar/*").forEach {
            val name = it.filename?.substringBeforeLast(".")
            if (name != null) avatars[name] = it
        }
    }

    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val left = sender.id
        val imgUrl = message.getImage()?.queryUrl()
        return when {
            imgUrl != null -> {
                val names = if (args.isEmpty()) sender.nameCardOrNick else sender.nameCardOrNick + " " + args[0]
                val right = HttpUtil.getInputStream(imgUrl)
                uploadImage(draw(left, right, names))
            }

            args.isEmpty() -> {
                val member = group.members.random()
                val names = sender.nameCardOrNick + " " + member.nameCardOrNick
                uploadImage(draw(left, member.id, names))
            }

            args[0] == "二次元" || args[0] == "动漫" -> {
                val name = avatars.keys.random()
                val names = sender.nameCardOrNick + " " + name
                val right = avatars[name]!!.inputStream
                draw(left, right, names).use { uploadImage(it) }
            }

            else -> when (val qq = args.getQq()) {
                null -> {
                    val avatar = avatars[args[0]] ?: return "没有找到角色".toPlainText()
                    val names = sender.nameCardOrNick + " " + args[0]
                    draw(left, avatar.inputStream, names).use { group.uploadImage(it) }
                }

                else -> {
                    val member = group.members[qq] ?: return "没有找到成员".toPlainText()
                    val names = sender.nameCardOrNick + " " + member.remarkOrNameCard
                    draw(left, qq, names).use { group.uploadImage(it) }
                }
            }
        }
    }

    private suspend fun draw(left: Long, right: Long, names: String): ByteArrayInputStream =
        draw(left, right.download(), names)

    private suspend fun draw(left: Long, right: InputStream, names: String): ByteArrayInputStream =
        left.download().use { leftIS ->
            right.use {
                when ((1..3).random()) {
                    1 -> draw1(leftIS, it, names)
                    2 -> draw2(leftIS, it, names)
                    else -> draw3(leftIS, it)
                }
            }
        }

    private fun draw1(left: InputStream, right: InputStream, names: String): ByteArrayInputStream {
        val imgLeft = ImageUtil.scaleTo(left, 160, 160)
        val imgRight = ImageUtil.scaleTo(right, 160, 160)
        val context = GraphicsContext {
            it.color = Color.BLACK
            it.font = Font("SimSun", Font.BOLD, 38)
        }
        val text = Text(names, 700, 633, context)
        return Canvas(bottom1).draw(text).image.overlay(imgLeft, 602, 100).overlay(imgRight, 768, 100).toStream()
    }

    private fun draw2(left: InputStream, right: InputStream, names: String): ByteArrayInputStream {
        val imgLeft = ImageUtil.scaleTo(left, 160, 160)
        val imgRight = ImageUtil.scaleTo(right, 160, 160)
        val context = GraphicsContext {
            it.color = Color.BLACK
            it.font = Font("SimSun", Font.BOLD, 30)
        }
        val text = Text(names, 300, 360, context)
        return Canvas(bottom2).draw(text).image.overlay(imgLeft, 575, 335).overlay(imgRight, 745, 335).toStream()
    }

    private fun draw3(left: InputStream, right: InputStream): ByteArrayInputStream {
        val imgLeft = ImageUtil.scaleTo(left, 90, 90)
        val imgRight = ImageUtil.scaleTo(right, 90, 90)
        return bottom3.overlay(imgLeft, 125, 275).overlay(imgRight, 220, 275).toStream()
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
