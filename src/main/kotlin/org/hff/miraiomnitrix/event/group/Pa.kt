package org.hff.miraiomnitrix.event.group

import com.sksamuel.scrimage.ImmutableImage
import net.mamoe.mirai.event.events.GroupMessageEvent
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.event.*
import org.hff.miraiomnitrix.utils.ImageUtil.toImmutableImage
import org.hff.miraiomnitrix.utils.ImageUtil.toStream
import org.hff.miraiomnitrix.utils.download
import org.hff.miraiomnitrix.utils.getQq
import org.hff.miraiomnitrix.utils.uploadImage
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import java.awt.BasicStroke
import java.awt.Color
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.io.InputStream
import java.util.concurrent.ThreadLocalRandom

@Event(priority = 5)
class Pa(private val permissionProperties: PermissionProperties) : GroupEvent {

    private val paDir = "/img/pa/*"
    private val tieDir = "/img/tie/*"

    override suspend fun GroupMessageEvent.handle(args: List<String>, isAt: Boolean): EventResult {
        if (args.isEmpty()) return stop()
        val pa = args.contains("爬")
        val tie = args.contains("贴")
        if (!pa && !tie) return next()
        val qq = if (isAt) sender.id else args.getQq() ?: return next()

        val imageA = getFileByQQ(qq, pa).toImmutableImage(400, 400)
        val imageB = qq.download().toImmutableImage(75, 75).transformAvatar()
        return imageA.overlay(imageB, 5, 320).toStream().let { uploadImage(it) }.let(::stop)
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

    private fun ImmutableImage.transformAvatar(): ImmutableImage {
        val bufferedImage = BufferedImage(height, height, BufferedImage.TYPE_4BYTE_ABGR)

        with(bufferedImage.createGraphics()) {
            setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            clip = Ellipse2D.Double(1.0, 1.0, height - 2.0, height - 2.0)
            drawImage(awt(), 1, 1, height - 2, height - 2, null)
            dispose()
        }

        with(bufferedImage.createGraphics()) {
            setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            stroke = BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            color = Color.WHITE
            drawOval(2, 2, height - 4, height - 4)
            dispose()
        }

        return ImmutableImage.fromAwt(bufferedImage)
    }
}
