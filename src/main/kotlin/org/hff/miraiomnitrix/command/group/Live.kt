package org.hff.miraiomnitrix.command.group

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.bot
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.GroupCommand
import org.hff.miraiomnitrix.db.entity.Live
import org.hff.miraiomnitrix.db.service.LiveService
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.getQq
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@EnableScheduling
@Command(name = ["直播", "live"])
class Live(private val liveService: LiveService) : GroupCommand {

    private val infoApi = "https://api.bilibili.com/x/space/wbi/acc/info?mid="

    val help = """
        |使用list、列表命令获取主播列表
        |使用add、添加命令来添加主播，格式为add qq号 b站uid
        |使用del、移除命令移除主播，格式为del qq号
    """.trimMargin()


    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        if (args.isEmpty()) {
            val list = liveService.ktQuery().eq(Live::groupId, group.id).list()
            if (list.isEmpty()) return "尚未添加主播\n使用live help指令获取使用方法".toPlainText()

            val liveStates = mutableListOf<String>()
            coroutineScope {
                list.forEach {
                    val member = group.getMember(it.qq) ?: return@forEach
                    launch {
                        val liveState = getLiveState(it.uid) ?: return@launch
                        liveStates.add(member.nick + "：" + liveState)
                    }
                }
            }
            if (liveStates.isNotEmpty()) return ("当前正在直播：\n" + liveStates.joinToString("\n")).toPlainText()
            return "当前没有人在直播".toPlainText()
        }

        return when (args[0]) {
            "帮助", "help" -> help

            "列表", "list" -> {
                val list = liveService.getListByGroup(group.id)
                if (list.isEmpty()) return "尚未添加主播".toPlainText()
                ("主播列表：\n" + list.mapNotNull { group.members[it.qq]?.nameCardOrNick }.joinToString("\n"))
            }

            "添加", "add", "save" -> {
                if (args.size < 3) return "参数错误".toPlainText()
                val qq = args[1].getQq() ?: return "qq号错误".toPlainText()
                val member = group.getMember(qq) ?: return "未找到成员".toPlainText()
                val count = liveService.ktQuery().eq(Live::qq, qq).eq(Live::groupId, group.id).count()
                if (count != 0L) return "人员重复".toPlainText()
                val uid = args[2].toLong()
                val userInfo = getBilibiliUserInfo(uid)
                val live = userInfo.live_room?.let { Live(null, qq, group.id, uid, it.roomid) }
                    ?: return "未找到直播间信息".toPlainText()
                val save = liveService.save(live)
                if (!save) return "保存失败".toPlainText()
                "已添加${member.nameCardOrNick}的数据"
            }
//            "订阅", "subscribe" -> {}
//            "更新", "update" -> {}
            "移除", "del", "remove" -> {
                if (args.size < 2) return "参数错误".toPlainText()
                val qq = args[1].getQq() ?: return "qq号错误".toPlainText()
                val member = group.getMember(qq) ?: return "未找到成员".toPlainText()
                val remove = liveService.removeByQQ(qq)
                if (!remove) return "删除失败".toPlainText()
                "已删除${member.nameCardOrNick}的数据"
            }

            else -> help
        }.toPlainText()
    }

    val livesCache = mutableMapOf<Long, Int>()

    @Scheduled(initialDelay = 60_000, fixedDelay = 90_000)
    fun listen() {
        runBlocking {
            val lives = liveService.list()
            lives.forEach {
                val cache = livesCache[it.qq]
                val userInfo = getBilibiliUserInfo(it.uid)
                val liveRoom = userInfo.live_room ?: return@forEach
                if (cache != null) {
                    val group = bot.groups[it.groupId] ?: return@forEach
                    if (cache == 0 && liveRoom.liveStatus == 1) {
                        group.sendMessage("${userInfo.name}开始直播，地址:${liveRoom.url}")
                    }
                }
                livesCache[it.qq] = liveRoom.liveStatus
            }
        }
    }

    suspend fun getLiveState(uid: Long): String? {
        val userInfo = getBilibiliUserInfo(uid)
        val (liveStatus, roomStatus, _, title, url) = userInfo.live_room ?: return null
        if (liveStatus != 1 || roomStatus != 1) return null
        val newUrl = removeStr(url)
        return "[$title] $newUrl"
    }

    private fun removeStr(str: String): String {
        val index = str.indexOf("?")
        return if (index > 0) str.take(index) else str
    }

    private suspend fun getBilibiliUserInfo(uid: Long): UserInfo = HttpUtil.getJson<Result>(infoApi + uid).data

    data class Result(val data: UserInfo)
    data class UserInfo(val name: String, val live_room: LiveRoom?)
    data class LiveRoom(
        val liveStatus: Int,
        val roomStatus: Int,
        val roomid: Int,
        val title: String,
        val url: String
    )
}
