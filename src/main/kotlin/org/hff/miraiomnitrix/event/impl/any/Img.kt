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
import org.hff.miraiomnitrix.utils.Util
import java.io.ByteArrayInputStream
import java.io.File

object Img : AnyHandler {

    private const val path1 = "./img/other/jjgw.jpg"
    private const val path2 = "./img/other/always.png"

    override suspend fun handle(sender: User, message: MessageChain, subject: Contact, args: List<String>): Boolean {
        if (args.isEmpty()) return false

        when (args[0]) {
            "急急国王" -> {
                val qqImg = Util.getQqImg(args, sender)
                val imageA = ImageUtil.scaleTo(File(path1), 400, 400)
                val imageB = ImageUtil.scaleTo(qqImg, 100, 125)
                val overlay = ImageUtil.overlay(imageA, imageB, 190, 5)
                subject.sendImage(overlay)
                return true
            }

            "一直" -> {
                val imageA = ImageUtil.scaleTo(File(path2), 400, 470)
                val image = ImageUtil.getFormCache(message)
                val imageB = if (image == null) {
                    val qqImg = Util.getQqImg(args, sender)
                    ImageUtil.scaleTo(qqImg, 340, 340)
                } else {
                    val img = HttpUtil.getInputStream(image.queryUrl())
                    ImageUtil.scaleTo(img, 340, 340)
                }
                val imageC = imageB.copy().scaleTo(60, 60)
                val overlayA = imageA.overlay(imageB, 30, 30)
                val overlayB = overlayA.overlay(imageC, 230, 390)
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