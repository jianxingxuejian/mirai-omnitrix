package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MusicKind
import net.mamoe.mirai.message.data.MusicShare
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.JsonUtil
import java.net.URLEncoder

@Command(name = ["音乐", "网易云", "music", "wyy", "点歌"])
class Music : AnyCommand {

    private val searchUrl = "http://music.163.com/api/search/get?type=1&limit=20&s="

    override suspend fun execute(args: List<String>, event: MessageEvent): Message? {
        if (args.isEmpty()) return "请输入歌曲名".toPlainText()
        val name = args.joinToString(" ")
        val encode = URLEncoder.encode(name, Charsets.UTF_8)
        val json = HttpUtil.getString(searchUrl + encode)
        val result: MusicResult = JsonUtil.fromJson(json, "result")
        val songs = result.songs
        if (songs.isEmpty()) return "未找到歌曲".toPlainText()
        val song = songs.find { it.status == 0 } ?: songs[0]
        val artists = song.artists
        return MusicShare(
            kind = MusicKind.NeteaseCloudMusic,
            title = song.name,
            summary = artists.joinToString("/") { it.name },
            jumpUrl = "https://y.music.163.com/m/song?id=${song.id}",
            pictureUrl = artists[0].img1v1Url,
            musicUrl = "http://music.163.com/song/media/outer/url?id=${song.id}",
            brief = "[分享]" + song.name,
        )
    }


    data class MusicResult(
        val hasMore: Boolean,
        val songCount: Int,
        val songs: List<Song>
    )

    data class Song(
        val album: Album,
        val alias: List<String>,
        val artists: List<ArtistX>,
        val copyrightId: Int,
        val duration: Int,
        val fee: Int,
        val ftype: Int,
        val id: Int,
        val mark: Long,
        val mvid: Int,
        val name: String,
        val rUrl: Any,
        val rtype: Int,
        val status: Int
    )

    data class Album(
        val alia: List<String>,
        val artist: ArtistX,
        val copyrightId: Int,
        val id: Int,
        val mark: Int,
        val name: String,
        val picId: Long,
        val publishTime: Long,
        val size: Int,
        val status: Int,
        val transNames: List<String>
    )

    data class ArtistX(
        val albumSize: Int,
        val alias: List<Any>,
        val fansGroup: Any,
        val id: Int,
        val img1v1: Int,
        val img1v1Url: String,
        val name: String,
        val picId: Int,
        val picUrl: Any,
        val trans: Any
    )
}
