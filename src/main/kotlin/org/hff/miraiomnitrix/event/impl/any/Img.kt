package org.hff.miraiomnitrix.event.impl.any

import com.sksamuel.scrimage.nio.PngWriter
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.event.type.AnyHandler
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.ImageUtil
import org.hff.miraiomnitrix.utils.ImageUtil.overlayToStream
import org.hff.miraiomnitrix.utils.Util
import org.springframework.core.io.ClassPathResource
import java.io.ByteArrayInputStream

object Img : AnyHandler {

    private const val path1 = "/img/other/jjgw.jpg"
    private const val path2 = "/img/other/always.png"

    override suspend fun handle(sender: User, message: MessageChain, subject: Contact, args: List<String>): Boolean {
        if (args.isEmpty()) return false

        val first = args[0]
        when {
            first.startsWith("急急国王") || first.endsWith("急急国王") -> {
                val qqImg = Util.getQqImg(args, sender)
                val file = ClassPathResource(path1).inputStream
                val imageA = ImageUtil.scaleTo(file, 400, 400)
                val imageB = ImageUtil.scaleTo(qqImg, 100, 125)
                val overlay = imageA.overlayToStream(imageB, 190, 5)
                subject.sendImage(overlay)
                return true
            }

            first.startsWith("一直[") || first.endsWith("]一直") -> {
                val file = ClassPathResource(path2).inputStream
                val imageA = ImageUtil.scaleTo(file, 400, 500)
                val image = ImageUtil.getFormCache(message)
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
                ImageUtil.imageCache.put(send.source.internalIds[0], upload.imageId)
                return true
            }
        }

        return false
    }

}