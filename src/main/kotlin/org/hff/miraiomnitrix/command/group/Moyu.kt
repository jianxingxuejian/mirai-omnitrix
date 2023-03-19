package org.hff.miraiomnitrix.command.group

import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.CommandResult
import org.hff.miraiomnitrix.command.GroupCommand
import org.hff.miraiomnitrix.utils.HttpUtil
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO

@Command(name = ["moyu", "摸鱼"])
class Moyu : GroupCommand {

    private val url = "https://api.j4u.ink/proxy/redirect/moyu/calendar/"

    override suspend fun execute(args: List<String>, event: GroupMessageEvent): CommandResult? {
        val formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        HttpUtil.getInputStream("$url$formattedDate.png")
            .use {
                ImageIO.read(it)
                    .let { image ->
                        ByteArrayOutputStream().use { baos ->
                            ImageIO.write(image, "png", baos)
                            event.group.sendImage(baos.toByteArray().toExternalResource())
                        }
                    }
            }
        return null
    }

}
