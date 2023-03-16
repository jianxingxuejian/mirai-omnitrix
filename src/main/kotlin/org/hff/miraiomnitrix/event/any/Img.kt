package org.hff.miraiomnitrix.event.any

import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import org.hff.miraiomnitrix.event.*
import org.hff.miraiomnitrix.utils.*
import org.hff.miraiomnitrix.utils.ImageUtil.toImmutableImage
import org.hff.miraiomnitrix.utils.ImageUtil.toStream

@Event(priority = 3)
class Img : AnyEvent {

    private val jjgw = ImageUtil.load("/img/other/jjgw.jpg", 400, 400)
    private val always = ImageUtil.load("/img/other/always.png", 400, 500)

    override suspend fun handle(args: List<String>, event: MessageEvent, isAt: Boolean): EventResult {
        if (args.isEmpty()) return next()

        val first = args[0]
        val (subject, sender, message) = event.getInfo()

        when (first) {
            "急急国王" -> {
                val qqImg = Util.getQqImg(args, sender).toImmutableImage(100, 125)
                jjgw.overlay(qqImg, 190, 5).toStream().use { subject.sendImage(it) }
                return stop()
            }

            "一直" -> {
                val image = Cache.getImgFromCache(message)
                val top = if (image == null) {
                    Util.getQqImg(args, sender)
                } else {
                    HttpUtil.getInputStream(image.queryUrl())
                }.toImmutableImage(400, 400)
                val bottom = top.copy().scaleTo(60, 60)
                always.overlay(top, 0, 0).overlay(bottom, 230, 420).toStream().use { subject.sendImage(it) }
                return stop()
            }
        }

        return next()
    }

}
