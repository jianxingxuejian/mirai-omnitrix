package org.hff.miraiomnitrix.event.group

import com.sksamuel.scrimage.ImmutableImage
import net.mamoe.mirai.event.events.GroupMessageEvent
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.event.*
import org.hff.miraiomnitrix.utils.ImageUtil.toImmutableImage
import org.hff.miraiomnitrix.utils.ImageUtil.toStream
import org.hff.miraiomnitrix.utils.Util
import org.hff.miraiomnitrix.utils.getInfo
import org.hff.miraiomnitrix.utils.sendImage
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import java.awt.BasicStroke
import java.awt.Color
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.io.InputStream
import java.util.concurrent.ThreadLocalRandom

@Event(priority = 3)
class Pa(private val permissionProperties: PermissionProperties) : GroupEvent {

    private val paDir = "/img/pa/*"
    private val tieDir = "/img/tie/*"

    override suspend fun handle(args: List<String>, event: GroupMessageEvent, isAt: Boolean): EventResult {
        if (args.isEmpty()) return next()
        val (group, sender) = event.getInfo()

        val qq = Util.getQq(args, sender)
        val pa = args.contains("爬")
        val tie = args.contains("贴")

        if (!pa && !tie) return next()

        val imageA = getFileByQQ(qq, pa).toImmutableImage(400, 400)
        val imageB = Util.getQqImg(qq).toImmutableImage(75, 75)
        val imageC = transformAvatar(imageB)
        imageA.overlay(imageC, 5, 320).toStream()
            .use { return stop(group.sendImage(it)) }
    }

    private fun getFileByQQ(qq: Long, pa: Boolean): InputStream {
        val admin = permissionProperties.admin
        val path =
            if (admin.isNotEmpty() && admin.contains(qq)) tieDir
            else if (pa) paDir
            else tieDir
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
