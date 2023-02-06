package org.hff.miraiomnitrix.event.group

import com.sksamuel.scrimage.ImmutableImage
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.config.BotProperties
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.result.EventResult
import org.hff.miraiomnitrix.result.EventResult.Companion.next
import org.hff.miraiomnitrix.result.EventResult.Companion.stop
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.ImageUtil
import org.hff.miraiomnitrix.utils.ImageUtil.overlayToStream
import org.hff.miraiomnitrix.utils.SpringUtil
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import java.awt.BasicStroke
import java.awt.Color
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.io.InputStream
import java.util.concurrent.ThreadLocalRandom

object Pa : GroupEvent {

    private val botProperties = SpringUtil.getBean(BotProperties::class)
    private val permissionProperties = SpringUtil.getBean(PermissionProperties::class)

    private const val url = "https://q1.qlogo.cn/g?b=qq&s=640&nk="

    private const val paDir = "/img/pa/*"
    private const val tieDir = "/img/tie/*"

    override suspend fun handle(
        sender: Member,
        message: MessageChain,
        group: Group,
        args: List<String>,
        event: GroupMessageEvent
    ): EventResult {
        if (!args.any { it.endsWith("爬") }) return next()

        var qq = args.find { it.startsWith("@") }?.substring(1)?.toLong()
        if (qq == null) {
            if (botProperties == null) return next()
            if (message.contentToString().contains("@" + botProperties.qq)) {
                qq = sender.id
            } else {
                return next()
            }
        }
        val file = getFileByQQ(qq)

        val qqImg = HttpUtil.getInputStream(url + qq)
        val imageA = ImageUtil.scaleTo(file, 400, 400)
        val imageB = ImageUtil.scaleTo(qqImg, 75, 75)
        val imageC = transformAvatar(imageB)
        val overlay = imageA.overlayToStream(imageC, 5, 320)
        group.sendImage(overlay)
        return stop()
    }

    private fun getFileByQQ(qq: Long): InputStream {
        val admin = permissionProperties?.admin
        val path = if (admin?.isNotEmpty() == true && admin.contains(qq)) tieDir else paDir
        val files = PathMatchingResourcePatternResolver().getResources(path)
        val randomInt = ThreadLocalRandom.current().nextInt(files.size)
        return files[randomInt].inputStream
    }

    private fun transformAvatar(avatar: ImmutableImage): ImmutableImage {
        val height = avatar.height
        val bufferedImage = BufferedImage(height, height, BufferedImage.TYPE_4BYTE_ABGR)

        val graphics1 = bufferedImage.createGraphics()
        graphics1.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics1.clip = Ellipse2D.Double(1.0, 1.0, height - 2.0, height - 2.0)
        graphics1.drawImage(avatar.awt(), 1, 1, height - 2, height - 2, null)
        graphics1.dispose()

        val graphics2 = bufferedImage.createGraphics()
        graphics2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics2.stroke = BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        graphics2.color = Color.WHITE
        graphics2.drawOval(2, 2, height - 4, height - 4)
        graphics2.dispose()

        return ImmutableImage.fromAwt(bufferedImage)
    }
}