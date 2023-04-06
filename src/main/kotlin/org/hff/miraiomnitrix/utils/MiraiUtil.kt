package org.hff.miraiomnitrix.utils

import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.ForwardMessageBuilder
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain

fun String.toImage() = Image(this)

fun ForwardMessageBuilder.add(message: MessageChain) = add(context.bot.id, context.bot.nameCardOrNick, message)

suspend fun MessageEvent.sendOrAt(isGroup: Boolean, message: String) =
    if (isGroup) subject.sendMessage(At(sender) + message)
    else subject.sendMessage(message)

suspend fun MessageEvent.sendOrAt(isGroup: Boolean, groupMessage: String, userMessage: String) =
    if (isGroup) subject.sendMessage(At(sender) + groupMessage)
    else subject.sendMessage(userMessage)

suspend fun MessageEvent.sendOrAt(message: String) = sendOrAt(this is GroupMessageEvent, message)
suspend fun MessageEvent.sendOrAt(groupMessage: String, userMessage: String) =
    sendOrAt(this is GroupMessageEvent, groupMessage, userMessage)
