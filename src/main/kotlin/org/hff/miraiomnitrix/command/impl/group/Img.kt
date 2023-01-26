package org.hff.miraiomnitrix.command.impl.group

import com.sksamuel.scrimage.nio.PngWriter
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.command.core.Command
import org.hff.miraiomnitrix.command.type.GroupCommand
import org.hff.miraiomnitrix.result.ResultMessage
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.ImageUtil
import org.hff.miraiomnitrix.utils.ImageUtil.getFormCache
import org.hff.miraiomnitrix.utils.ImageUtil.imageCache
import org.hff.miraiomnitrix.utils.Util.getQqImg
import java.io.ByteArrayInputStream
import java.io.File

@Command(name = ["img", "表情包"], isNeedHeader = false)
class Img : GroupCommand {

    private val path1 = "./img/other/jjgw.jpg"
    private val path2 = "./img/other/always.png"
    override suspend fun execute(
        sender: Member,
        message: MessageChain,
        group: Group,
        args: List<String>
    ): ResultMessage? {
        when (args[0]) {
            "急急国王" -> {
                val qqImg = getQqImg(args, sender)
                val imageA = ImageUtil.scaleTo(File(path1), 400, 400)
                val imageB = ImageUtil.scaleTo(qqImg, 100, 125)
                val overlay = ImageUtil.overlay(imageA, imageB, 190, 5)
                group.sendImage(overlay)
            }

            "一直" -> {
                val imageA = ImageUtil.scaleTo(File(path2), 400, 470)
                val image = getFormCache(message)
                val imageB = if (image == null) {
                    val qqImg = getQqImg(args, sender)
                    ImageUtil.scaleTo(qqImg, 340, 340)
                } else {
                    val img = HttpUtil.getInputStream(image.queryUrl())
                    ImageUtil.scaleTo(img, 340, 340)
                }
                val imageC = imageB.copy().scaleTo(60, 60)
                val overlayA = imageA.overlay(imageB, 30, 30)
                val overlayB = overlayA.overlay(imageC, 230, 390)
                val inputStream = ByteArrayInputStream(overlayB.bytes(PngWriter()))
                val upload = group.uploadImage(inputStream)
                val send = group.sendMessage(upload)
                imageCache.put(send.source.internalIds[0], upload.imageId)
            }
        }

        return null
    }
}