package org.hff.miraiomnitrix.utils

import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent

object Util {
    private const val QQ_URL = "https://q1.qlogo.cn/g?b=qq&s=640&nk="
    private const val BILIBILI_INFO_API = "https://api.bilibili.com/x/space/wbi/acc/info?mid="

    fun getInfo(event: MessageEvent) = Triple(event.sender, event.message, event.subject)

    fun getInfo(event: GroupMessageEvent) = Triple(event.sender, event.message, event.group)

    fun getInfo(event: FriendMessageEvent) = Pair(event.friend, event.message)

    fun getQq(args: List<String>) = args.find { it.startsWith("@") }?.substring(1)?.toLong()

    fun getQq(args: List<String>, sender: User) = getQq(args) ?: sender.id

    fun getQq(arg: String) = if (arg.startsWith("@")) arg.substring(1).toLong() else arg.toLong()

    fun getQqImg(args: List<String>) = HttpUtil.getInputStream(QQ_URL + getQq(args))

    fun getQqImg(args: List<String>, sender: User) = HttpUtil.getInputStream(QQ_URL + getQq(args, sender))

    fun getBilibiliUserInfo(uid: Long): UserInfo {
        val apiResult = HttpUtil.getString(BILIBILI_INFO_API + uid)
        return JsonUtil.fromJson(apiResult, "data", UserInfo::class)
    }

    data class UserInfo(val name: String, val live_room: LiveRoom?)

    data class LiveRoom(
        val liveStatus: Int,
        val roomStatus: Int,
        val roomid: Int,
        val title: String,
        val url: String
    )
}
