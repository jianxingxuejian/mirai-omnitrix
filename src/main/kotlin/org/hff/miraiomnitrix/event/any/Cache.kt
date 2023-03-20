package org.hff.miraiomnitrix.event.any

import com.google.common.cache.CacheBuilder
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.source
import org.hff.miraiomnitrix.event.*
import org.hff.miraiomnitrix.utils.getInfo
import java.util.concurrent.TimeUnit

@Event(priority = 5)
class Cache : AnyEvent {

    companion object {
        val imageCache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).build<Int, String>()
        val errorCache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).build<Long, Exception>()
    }

    override suspend fun handle(args: List<String>, event: MessageEvent, isAt: Boolean): EventResult {
        val (subject, _, message) = event.getInfo()
        if (args.getOrNull(0) == "error") {
            val stackTrace = errorCache.getIfPresent(event.sender.id)?.stackTrace ?: return next("未找到错误")
            if (stackTrace.isEmpty()) return next("未找到错误")
            val maxLines = (args.getOrNull(1)?.toIntOrNull() ?: 10).coerceIn(1, stackTrace.size)
            subject.sendMessage(stackTrace.take(maxLines).joinToString("\n"))
            return stop()
        }
        message[Image.Key]?.run { imageCache.put(message.source.internalIds[0], imageId) }
        return next()
    }
}

fun MessageChain.getImageFromCache(): Image? =
    when (val quote = this[QuoteReply.Key]) {
        null -> this[Image.Key]
        else -> Cache.imageCache.getIfPresent(quote.source.internalIds[0])?.let { Image(it) }
    }
