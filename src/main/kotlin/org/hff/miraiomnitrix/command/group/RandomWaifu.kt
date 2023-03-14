package org.hff.miraiomnitrix.command.group

import com.sksamuel.scrimage.ImmutableImage
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.event.events.GroupMessageEvent
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.CommandResult
import org.hff.miraiomnitrix.command.GroupCommand
import org.hff.miraiomnitrix.command.result
import org.hff.miraiomnitrix.utils.*
import org.hff.miraiomnitrix.utils.ImageUtil.toStream
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import java.io.ByteArrayInputStream
import java.io.File

@Command(name = ["随机老婆", "老婆", "waifu"])
class RandomWaifu : GroupCommand {

    lateinit var imgBottom: ImmutableImage

    private val genshinAvatars = hashMapOf<String, ByteArray>()

    init {
        imgBottom = ImageUtil.load(ClassPathResource("/img/other/2.jpg").file)
        val files = PathMatchingResourcePatternResolver().getResources("/img/genshin/avatar/*")
        files.forEach {
            val name = it.filename?.substringBeforeLast(".")
            if (name != null) {
                genshinAvatars[name] = it.inputStream.readBytes()
            }
        }
    }

    override suspend fun execute(args: List<String>, event: GroupMessageEvent): CommandResult? {
        val (group, sender) = event.getInfo()
        val left = sender.id
        if (args.isEmpty()) {
            val right = group.members.random().id
            draw(left, right).use { group.sendImage(it) }
        } else if (args[0] == "原神") {
            val right = genshinAvatars.values.random()
            draw(left, right).use { group.sendImage(it) }
        } else {
            val qq = Util.getQq(args)
            if (qq != null) {
                draw(left, qq).use { group.sendImage(it) }
            } else {
                val right = genshinAvatars[args[0]]
                if (right != null) {
                    draw(left, right).use { group.sendImage(it) }
                } else {
                    return result("没有找到角色")
                }
            }
        }
        return null
    }

    private fun draw(left: Long, right: Long): ByteArrayInputStream {
        val inputStreamLeft = Util.getQqImg(left)
        val inputStreamRight = Util.getQqImg(right)
        return draw(inputStreamLeft.readAllBytes(), inputStreamRight.readAllBytes())
    }

    private fun draw(left: Long, right: ByteArray): ByteArrayInputStream {
        val inputStreamLeft = Util.getQqImg(left)
        return draw(inputStreamLeft.readAllBytes(), right)
    }

    private fun draw(left: ByteArray, right: ByteArray): ByteArrayInputStream {
        val imgLeft = ImageUtil.scaleTo(left, 150, 180)
        val imgRight = ImageUtil.scaleTo(right, 150, 180)
        return imgBottom.overlay(imgLeft, 610, 90).overlay(imgRight, 770, 90).toStream()
    }

}

fun main() {
    val json =
        HttpUtil.getString("https://api-static.mihoyo.com/common/blackboard/ys_obc/v1/home/content/list?app_sn=ys_obc&channel_id=189")
    JsonUtil.getObj(json, "data").getAsArray("list")[0].getAsArray("children")[0].getAsArray("list").forEach {
        val icon = it.getAsStr("icon")
        val img = HttpUtil.getInputStream(icon)
        val title = it.getAsStr("title")
        val filename = "src/main/resources/img/genshin/avatar/$title.png"
        val file = File(filename)
        file.outputStream().use { output -> img.copyTo(output) }
    }
}



