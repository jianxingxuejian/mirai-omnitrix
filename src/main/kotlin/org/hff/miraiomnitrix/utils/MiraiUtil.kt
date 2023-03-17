package org.hff.miraiomnitrix.utils

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.ForwardMessageBuilder
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import org.hff.miraiomnitrix.event.any.Cache
import java.io.InputStream

fun MessageEvent.getInfo() = Triple(this.subject, this.sender, this.message)

fun GroupMessageEvent.getInfo() = Triple(this.group, this.sender, this.message)

fun String.toImage() = Image(this)

fun String.toText() = PlainText(this)

fun ForwardMessageBuilder.add(message: MessageChain) = this.add(context.bot.id, context.bot.nameCardOrNick, message)

suspend fun Contact.sendImageAndCache(inputStream: InputStream): Image {
    val image = this.uploadImage(inputStream)
    this.sendImageAndCache(image)
    return image
}

suspend fun Contact.sendImageAndCache(image: Image) {
    val send = this.sendMessage(image)
    Cache.imageCache.put(send.source.internalIds[0], image.imageId)
}

suspend fun Contact.sendMessage(forwardMessageBuilder: ForwardMessageBuilder) =
    this.sendMessage(forwardMessageBuilder.build())
