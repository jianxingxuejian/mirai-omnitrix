package org.hff.miraiomnitrix.event.any

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

        val (subject, sender, message) = event.getInfo()

        when (args[0]) {
            "急急国王" -> {
                val qqAvatar = (if (args.size == 1) sender.id else args.getQq())
                    ?.toAvatar()?.use { it.toImmutableImage(100, 125) } ?: return next()
                jjgw.overlay(qqAvatar, 190, 5).toStream().use { subject.sendImageAndCache(it) }
                return stop()
            }

            "一直" -> {
                val top = when (val image = message.getImageFromCache()) {
                    null -> (if (args.size == 1) sender.id else args.getQq())
                        ?.toAvatar() ?: return next()

                    else -> HttpUtil.getInputStream(image.queryUrl())
                }.use { it.toImmutableImage(400, 400) }

                val bottom = top.copy().scaleTo(60, 60)
                always.overlay(top, 0, 0).overlay(bottom, 230, 420).toStream()
                    .use { subject.sendImageAndCache(it) }
                return stop()
            }

            else -> {}
        }
        return next()
    }

}
