package org.hff.miraiomnitrix.event.any

import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import org.hff.miraiomnitrix.common.getImage
import org.hff.miraiomnitrix.event.*
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.ImageUtil
import org.hff.miraiomnitrix.utils.ImageUtil.toImmutableImage
import org.hff.miraiomnitrix.utils.ImageUtil.toStream
import org.hff.miraiomnitrix.utils.getQq
import org.hff.miraiomnitrix.utils.toAvatar

@Event(priority = 2)
class Img : AnyEvent {

    private val jjgw = ImageUtil.load("/img/other/jjgw.jpg", 400, 400)
    private val always = ImageUtil.load("/img/other/always.png", 400, 500)

    override suspend fun MessageEvent.handle(args: List<String>, isAt: Boolean): EventResult {
        if (args.isEmpty()) return stop()

        return when (args[0]) {
            "急急国王" -> {
                val qqAvatar = (if (args.size == 1) sender.id else args.getQq())
                    ?.toAvatar()?.use { it.toImmutableImage(100, 125) } ?: return next()
                jjgw.overlay(qqAvatar, 190, 5).toStream().use { subject.uploadImage(it) }.run(::stop)
            }

            "一直" -> {
                val top = when (val image = message.getImage()) {
                    null -> (if (args.size == 1) sender.id else args.getQq())
                        ?.toAvatar() ?: return next()

                    else -> HttpUtil.getInputStream(image.queryUrl())
                }.use { it.toImmutableImage(400, 400) }

                val bottom = top.copy().scaleTo(60, 60)
                always.overlay(top, 0, 0).overlay(bottom, 230, 420).toStream()
                    .use { subject.uploadImage(it) }.run(::stop)
            }

            else -> next()
        }
    }

}
