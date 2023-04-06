package org.hff.miraiomnitrix.common

import com.google.common.cache.CacheBuilder
import com.google.common.collect.EvictingQueue
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import java.util.concurrent.TimeUnit

/** 图片缓存，只保留2小时. key是消息id,value是图片id */
private val imageCache = CacheBuilder
    .newBuilder()
    .expireAfterWrite(120, TimeUnit.MINUTES)
    .build<Int, String>()

/** 从消息链中获取消息id，再从缓存获取图片id */
fun MessageChain.getImage(): Image? =
    when (val quote = this[QuoteReply.Key]) {
        null -> this[Image.Key]
        else -> imageCache.getIfPresent(quote.source.internalIds[0])?.let(::Image)
    }

/** 将消息链中的图片缓存下来 */
fun MessageEvent.putImage() {
    val image = message[Image.Key] ?: return
    imageCache.put(message.source.internalIds[0], image.imageId)
}


/** 消息回执缓存，key是subjectId，只保留最近的10条 */
private val receiptCache = hashMapOf<Long, EvictingQueue<MessageReceipt<Contact>>>()

/** 发送消息并缓存回执 */
suspend fun Contact.sendAndCache(message: Message?) {
    if (message == null) return
    val receipt = sendMessage(message)
    if (message is Image) imageCache.put(receipt.source.internalIds[0], message.imageId)
    receiptCache.getOrPut(id) { EvictingQueue.create(10) }.apply { add(receipt) }
}

/**
 * 撤回已发送消息
 *
 * @param subjectId 消息对象id
 * @param num 撤回数量
 */
suspend fun recallLastMessage(subjectId: Long, num: Int) {
    val queue = receiptCache[subjectId] ?: return
    queue.toList().takeLast(num).forEach {
        try {
            it.recall()
        } finally {
            queue.remove(it)
        }
    }
}


/** 错误缓存,key是subjectId,value是错误详情 */
val errorCache = CacheBuilder.newBuilder().maximumSize(100).build<Long, Exception>()

