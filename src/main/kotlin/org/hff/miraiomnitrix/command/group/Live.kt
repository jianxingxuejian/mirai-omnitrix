package org.hff.miraiomnitrix.command.group

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.bot
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.GroupCommand
import org.hff.miraiomnitrix.common.MyException
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
    private val statusApi = "https://api.live.bilibili.com/room/v1/Room/get_status_info_by_uids"

    val help = """
        |使用list、列表命令获取主播列表
        |使用add、添加命令来添加主播，格式为add qq号 b站uid
        |使用del、移除命令移除主播，格式为del qq号
    """.trimMargin()


    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        if (args.isEmpty()) {
            val list = liveService.ktQuery().eq(Live::groupId, group.id).list()
            if (list.isEmpty()) return "尚未添加主播\n使用live help指令获取使用方法".toPlainText()

            val datas = getLiveStates(list.map { it.uid })
            return if (datas.isNullOrEmpty()) {
                "当前没有人在直播"
            } else {
                "当前正在直播：\n" + datas.map { "UP：${it.uname}，标题：${it.title},地址：https://live.bilibili.com/${it.room_id}" }
            }.toPlainText()
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
                val live = userInfo.live_room?.run { Live(null, qq, group.id, uid, roomid) }
                    ?: return "未找到直播间信息".toPlainText()
                val save = liveService.save(live)
                if (!save) return "保存失败".toPlainText()
                "已添加${member.nameCardOrNick}的数据"
            }

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

    @Scheduled(initialDelay = 30_000, fixedDelay = 30_000)
    fun listen() {
        runBlocking {
            liveService.list().groupBy { it.groupId }.entries.forEach { (groupId, lives) ->
                val datas = getLiveStates(lives.map { it.uid })
                if (datas.isNullOrEmpty()) return@forEach
                val group = bot.groups[groupId] ?: return@forEach
                for (data in datas) {
                    val cache = livesCache[data.uid]
                    if (cache == 0 && data.live_status == 1) {
                        group.sendMessage("${data.uname}开始直播，地址：https://live.bilibili.com/${data.room_id}")
                    }
                    livesCache[data.uid] = data.live_status
                }
            }
        }
    }

    private val headers = mapOf("User-Agent" to "PostmanRuntime/7.26.8")

    suspend fun getLiveStates(uids: List<Long>): List<Data>? {
        if (uids.isEmpty()) return null
        val data = mapOf("uids" to uids)
        val result: LiveResult = HttpUtil.postJson<LiveResult>(statusApi, data, headers)
        if (result.code != 0) throw MyException("请求失败")
        return result.data.values.filter { it.live_status == 1 }
    }

    private suspend fun getBilibiliUserInfo(uid: Long): UserInfo {
        val result = HttpUtil.getJson<UserInfoResult?>(infoApi + uid, headers) ?: throw MyException("调用api失败")
        return result.data
    }

    data class UserInfoResult(val data: UserInfo)
    data class UserInfo(val name: String, val live_room: LiveRoom?)
    data class LiveRoom(val roomid: Int)

    data class LiveResult(val code: Int, val `data`: Map<String, Data>)
    data class Data(
        val title: String,
        val room_id: Long,
        val uid: Long,
        val live_status: Int,
        val uname: String,
    )
}
