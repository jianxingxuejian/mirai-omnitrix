package org.hff.miraiomnitrix.listener

import com.google.common.cache.CacheBuilder
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.source
import org.hff.miraiomnitrix.command.core.CommandManager
import org.hff.miraiomnitrix.event.Chat.chat
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

object AnyListener : SimpleListenerHost() {

    val imageCache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).build<Int, String>()

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        exception.printStackTrace()
    }

    @EventHandler
    suspend fun MessageEvent.onMessage() {
        val first = message[1]
        if (first is PlainText) chat(first.content, subject)

        val image = message[Image.Key]
        if (image != null) {
            imageCache.put(message.source.internalIds[0], image.imageId)
        }

        val (msg, message) = CommandManager.executeAnyCommand(sender, message, subject) ?: return
        if (msg != null) {
            subject.sendMessage(msg)
        }
        if (message != null) {
            subject.sendMessage(message)
        }
    }
}