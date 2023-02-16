package org.hff.miraiomnitrix.utils

import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent

object Util {
    private const val QQ_URL = "https://q1.qlogo.cn/g?b=qq&s=640&nk="

    fun getQq(args: List<String>) = args.find { it.startsWith("@") }?.substring(1)?.toLong()

    fun getQq(args: List<String>, sender: User) = getQq(args) ?: sender.id

    fun getQq(arg: String) = if (arg.startsWith("@")) arg.substring(1).toLong() else arg.toLong()

    fun getQqImg(args: List<String>) = HttpUtil.getInputStream(QQ_URL + getQq(args))

    fun getQqImg(args: List<String>, sender: User) = HttpUtil.getInputStream(QQ_URL + getQq(args, sender))

}

fun MessageEvent.getInfo() = Triple(this.subject, this.sender, this.message)

fun GroupMessageEvent.getInfo() = Triple(this.group, this.sender, this.message)

fun FriendMessageEvent.getInfo() = Pair(this.friend, this.message)
