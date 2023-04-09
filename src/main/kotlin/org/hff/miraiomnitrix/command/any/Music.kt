package org.hff.miraiomnitrix.command.any

import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.nextMessage
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.send
import org.hff.miraiomnitrix.utils.toTime
import org.hff.miraiomnitrix.utils.toUrl
import kotlin.collections.set

@Command(name = ["音乐", "网易云", "music", "wyy", "点歌"])
class Music : AnyCommand {

    private val searchUrl = "http://music.163.com/api/search/get?type=1&limit=10&s="

    private val jobMap = hashMapOf<Long, Job>()

    override suspend fun MessageEvent.execute(args: List<String>): Message? {
        if (args.isEmpty()) return At(sender) + "请输入歌曲名"
        val name = args.joinToString(" ").toUrl()
        val songs = HttpUtil.getJson<Result>(searchUrl + name).result.songs
        if (songs.isNullOrEmpty()) return "未找到歌曲".toPlainText()
        val list = songs.mapIndexed { index, song ->
            val artists = song.artists
            val artist = artists.joinToString("/") { it.name }
            val length = song.duration.toTime()
            "${index + 1}. ${song.name} - $artist  $length"
        }
        subject.sendMessage(list.joinToString("\n"))

        try {
            jobMap[sender.id]?.cancel()
            coroutineScope {
                launch {
                    while (isActive) {
                        val next = nextMessage(30_000L, EventPriority.HIGH, intercept = true)
                        val num = next.content.toIntOrNull() ?: continue
                        if (num !in 1..list.size) subject.sendMessage("请输入有效的序号")
                        val song = songs[num - 1]
                        val artists = song.artists
                        MusicShare(
                            kind = MusicKind.NeteaseCloudMusic,
                            title = song.name,
                            summary = artists.joinToString("/") { it.name },
                            jumpUrl = "https://y.music.163.com/m/song?id=${song.id}",
                            pictureUrl = artists[0].img1v1Url,
                            musicUrl = "http://music.163.com/song/media/outer/url?id=${song.id}",
                            brief = "[分享]" + song.name,
                        ).let { send(it) }
                    }
                }.also { jobMap[sender.id] = it }
            }
        } finally {
            jobMap.remove(sender.id)
        }

        return null
    }

    data class Result(val code: Int, val result: MusicResult)
    data class MusicResult(val hasMore: Boolean, val songCount: Int, val songs: List<Song>?)
    data class Song(val artists: List<ArtistX>, val duration: Int, val id: Int, val name: String)
    data class ArtistX(val img1v1Url: String, val name: String)

}
