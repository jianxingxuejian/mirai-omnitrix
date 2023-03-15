package org.hff.miraiomnitrix.event.any

import com.sksamuel.scrimage.nio.PngWriter
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import org.hff.miraiomnitrix.event.*
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.ImageUtil
import org.hff.miraiomnitrix.utils.ImageUtil.overlayToStream
import org.hff.miraiomnitrix.utils.Util
import org.hff.miraiomnitrix.utils.getInfo
import org.springframework.core.io.ClassPathResource
import java.io.ByteArrayInputStream

@Event(priority = 3)
class Img : AnyEvent {

    private val path1 = "/img/other/jjgw.jpg"
    private val path2 = "/img/other/always.png"

    override suspend fun handle(args: List<String>, event: MessageEvent, isAt: Boolean): EventResult {
        if (args.isEmpty()) return next()

        val first = args[0]
        val (subject, sender, message) = event.getInfo()

        when (first) {
            "急急国王" -> {
                val qqImg = Util.getQqImg(args, sender)
                val file = ClassPathResource(path1).inputStream
                val imageA = ImageUtil.scaleTo(file, 400, 400)
                val imageB = ImageUtil.scaleTo(qqImg, 100, 125)
                val overlay = imageA.overlayToStream(imageB, 190, 5)
                subject.sendImage(overlay)
                return stop()
            }

            "一直" -> {
                val file = ClassPathResource(path2).inputStream
                val imageA = ImageUtil.scaleTo(file, 400, 500)
                val image = Cache.getImgFromCache(message)
                val imageB = if (image == null) {
                    val qqImg = Util.getQqImg(args, sender)
                    ImageUtil.scaleTo(qqImg, 400, 400)
                } else {
                    val img = HttpUtil.getInputStream(image.queryUrl())
                    ImageUtil.scaleTo(img, 400, 400)
                }
                val imageC = imageB.copy().scaleTo(60, 60)
                val overlayA = imageA.overlay(imageB, 0, 0)
                val overlayB = overlayA.overlay(imageC, 230, 420)
                val inputStream = ByteArrayInputStream(overlayB.bytes(PngWriter()))
                val upload = subject.uploadImage(inputStream)
                val send = subject.sendMessage(upload)
                Cache.imageCache.put(send.source.internalIds[0], upload.imageId)
                return stop()
            }
        }

        return next()
    }

}
