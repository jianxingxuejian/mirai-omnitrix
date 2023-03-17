package org.hff.miraiomnitrix.command.group

import com.sksamuel.scrimage.canvas.Canvas
import com.sksamuel.scrimage.canvas.GraphicsContext
import com.sksamuel.scrimage.canvas.drawables.Text
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.contact.remarkOrNameCard
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.QuoteReply
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.CommandResult
import org.hff.miraiomnitrix.command.GroupCommand
import org.hff.miraiomnitrix.command.result
import org.hff.miraiomnitrix.event.any.Cache
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
            if (name != null) {
                avatars[name] = it
            }
        }
    }

    override suspend fun execute(args: List<String>, event: GroupMessageEvent): CommandResult? {
        val (group, sender, message) = event.getInfo()
        val left = sender.id
        val quote = message[QuoteReply.Key]
        val imgUrl =
            if (quote != null) Cache.imageCache.getIfPresent(quote.source.internalIds[0])?.let { Image(it).queryUrl() }
            else message[Image.Key]?.queryUrl()
        when {
            imgUrl != null -> {
                val names = if (args.isEmpty()) sender.nameCardOrNick else sender.nameCardOrNick + " " + args[0]
                HttpUtil.getInputStream(imgUrl)
                    .use { draw(left, it, names).use { img -> group.sendImageAndCache(img) } }
            }

            args.isEmpty() -> {
                val member = group.members.random()
                val names = sender.nameCardOrNick + " " + member.nameCardOrNick
                draw(left, member.id, names).use { group.sendImageAndCache(it) }
            }

            args[0] == "二次元" || args[0] == "动漫" -> {
                val name = avatars.keys.random()
                val names = sender.nameCardOrNick + avatars.keys.random()
                avatars[name]!!.inputStream.use { draw(left, it, names).use { img -> group.sendImageAndCache(img) } }
            }

            else -> {
                when (val qq = Util.getQq(args)) {
                    null -> {
                        val avatar = avatars[args[0]] ?: return result("没有找到角色")
                        val names = sender.nameCardOrNick + " " + args[0]
                        avatar.inputStream.use { draw(left, it, names).use { img -> group.sendImageAndCache(img) } }
                    }

                    else -> {
                        val member = group.members[qq] ?: return result("没有找到成员")
                        val names = sender.nameCardOrNick + " " + member.remarkOrNameCard
                        draw(left, qq, names).use { group.sendImageAndCache(it) }
                    }
                }
            }
        }
        return null
    }

    private fun draw(left: Long, right: Long, names: String): ByteArrayInputStream {
        val rightByteArray = Util.getQqImg(right)
        return draw(left, rightByteArray, names)
    }

    private fun draw(left: Long, rightIs: InputStream, names: String): ByteArrayInputStream {
        val leftIs = Util.getQqImg(left)
        return when ((1..3).random()) {
            1 -> draw1(leftIs, rightIs, names)
            2 -> draw2(leftIs, rightIs, names)
            else -> draw3(leftIs, rightIs)
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



