package org.hff.miraiomnitrix.listener

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.source
import org.hff.miraiomnitrix.command.core.CommandManager
import org.hff.miraiomnitrix.command.core.CommandManager.errorCache
import org.hff.miraiomnitrix.event.Chat
import org.hff.miraiomnitrix.utils.ImageUtil.imageCache
import kotlin.coroutines.CoroutineContext

object AnyListener : SimpleListenerHost() {

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        exception.printStackTrace()
    }

    @EventHandler
    suspend fun MessageEvent.onMessage() {
        val first = message[1]
        if (first is PlainText) {
            if (first.content == "error") {
                val error = errorCache.getIfPresent(subject.id) ?: return
                subject.sendMessage(error.stackTrace.toString())
                return
            } else Chat.chat(first.content, subject)
        }

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