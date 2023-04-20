package org.hff.miraiomnitrix.utils

import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.io.InputStream

/** 图片id转图片 */
fun String.toImage() = Image(this)

/** 添加一条转发信息 */
fun ForwardMessageBuilder.add(message: MessageChain) = add(context.bot.id, context.bot.nameCardOrNick, message)

/** 发送一条消息 */
suspend inline fun MessageEvent.send(message: String) = send(message.toPlainText())

/** 发送一条消息 */
suspend inline fun MessageEvent.send(message: Message) = subject.sendMessage(message)

/** 判断消息类型，如果是群则@发送人 */
suspend fun MessageEvent.sendOrAt(isGroup: Boolean, message: String) =
    if (isGroup) subject.sendMessage(At(sender) + message)
    else subject.sendMessage(message)

/** @see sendOrAt */
suspend fun MessageEvent.sendOrAt(isGroup: Boolean, groupMessage: String, userMessage: String) =
    if (isGroup) subject.sendMessage(At(sender) + groupMessage)
    else subject.sendMessage(userMessage)

/** @see sendOrAt */
suspend fun MessageEvent.sendOrAt(message: String) = sendOrAt(this is GroupMessageEvent, message)

/** @see sendOrAt */
suspend fun MessageEvent.sendOrAt(groupMessage: String, userMessage: String) =
    sendOrAt(this is GroupMessageEvent, groupMessage, userMessage)

/** 上传图片 */
suspend fun MessageEvent.uploadImage(inputStream: InputStream) = inputStream.use { subject.uploadImage(it) }

/** 上传图片 */
suspend fun MessageEvent.uploadImage(resource: ExternalResource) = resource.use { subject.uploadImage(it) }

/** 上传图片 */
suspend fun MessageEvent.uploadImage(byteArray: ByteArray, formatName: String? = null) =
    uploadImage(byteArray.toExternalResource(formatName))

/** 上传图片并引用消息 */
suspend fun MessageEvent.uploadImageAndQuote(inputStream: InputStream) = message.quote() + uploadImage(inputStream)
