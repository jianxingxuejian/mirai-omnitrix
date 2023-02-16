package org.hff.miraiomnitrix.event.any

import com.google.common.cache.CacheBuilder
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.source
import org.hff.miraiomnitrix.event.AnyEvent
import org.hff.miraiomnitrix.event.Event
import org.hff.miraiomnitrix.event.EventResult
import org.hff.miraiomnitrix.event.EventResult.Companion.next
import java.util.concurrent.TimeUnit

@Event(priority = 2)
class Cache : AnyEvent {

    companion object {

        val imageCache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).build<Int, String>()

        fun getImgFromCache(message: MessageChain): Image? {
            val quote = message[QuoteReply.Key]
            return if (quote != null) {
                val imageId = imageCache.getIfPresent(quote.source.internalIds[0])
                if (imageId == null) null
                else Image(imageId)
            } else message[Image.Key]
        }
    }

    override suspend fun handle(args: List<String>, event: MessageEvent): EventResult {
        val message = event.message
        val image = message[Image.Key] ?: return next()
        imageCache.put(message.source.internalIds[0], image.imageId)
        return next()
    }
}