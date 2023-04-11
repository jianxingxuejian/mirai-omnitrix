package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.withUse
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.uploadImage
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO

@Command(name = ["moyu", "摸鱼"])
class Moyu : AnyCommand {

    private val url = "https://api.j4u.ink/proxy/redirect/moyu/calendar/"

    override suspend fun MessageEvent.execute(args: List<String>): Message? {
        val formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        HttpUtil.getInputStream("$url$formattedDate.png").withUse(ImageIO::read).let { image ->
            ByteArrayOutputStream().use { baos ->
                ImageIO.write(image, "png", baos)
                return uploadImage(baos.toByteArray())
            }
        }
    }

}
