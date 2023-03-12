package org.hff.miraiomnitrix.utils

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.PlainText

fun MessageEvent.getInfo() = Triple(this.subject, this.sender, this.message)

fun GroupMessageEvent.getInfo() = Triple(this.group, this.sender, this.message)

fun String.toImage() = Image(this)

fun String.toText() = PlainText(this)