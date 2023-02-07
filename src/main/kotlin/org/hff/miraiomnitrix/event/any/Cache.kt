package org.hff.miraiomnitrix.event.any

import com.google.common.cache.CacheBuilder
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.source
import org.hff.miraiomnitrix.result.EventResult
import org.hff.miraiomnitrix.result.EventResult.Companion.next
import java.util.concurrent.TimeUnit

object Cache : AnyEvent {

    val imageCache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).build<Int, String>()

    override suspend fun handle(
        sender: User,
        message: MessageChain,
        subject: Contact,
        args: List<String>,
        event: MessageEvent
    ): EventResult {
        val image = message[Image.Key] ?: return next()
        imageCache.put(message.source.internalIds[0], image.imageId)
        return next()
    }

    fun getImgFromCache(message: MessageChain): Image? {
        val quote = message[QuoteReply.Key]
        return if (quote != null) {
            val imageId = imageCache.getIfPresent(quote.source.internalIds[0])
            if (imageId == null) null
            else Image(imageId)
        } else message[Image.Key]
    }
}