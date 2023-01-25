package org.hff.miraiomnitrix.command.impl.group

import cn.hutool.core.util.RandomUtil
import com.sksamuel.scrimage.ImmutableImage
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.command.core.Command
import org.hff.miraiomnitrix.command.type.GroupCommand
import org.hff.miraiomnitrix.config.BotProperties
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.result.ResultMessage
import org.hff.miraiomnitrix.result.result
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.ImageUtil
import java.awt.BasicStroke
import java.awt.Color
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.io.File


@Command(name = ["爬"], isNeedHeader = false)
class Pa(
    private val permissionProperties: PermissionProperties,
    private val botProperties: BotProperties
) : GroupCommand {

    private val url = "https://q1.qlogo.cn/g?b=qq&s=640&nk="

    private val paDir = "./img/pa"
    private val tieDir = "./img/tie"

    override suspend fun execute(
        sender: Member,
        message: MessageChain,
        group: Group,
        args: List<String>
    ): ResultMessage? {
        var qq = args.find { it.startsWith("@") }?.substring(1)?.toLong()
        if (qq == null) {
            if (message.contentToString().contains("@" + botProperties.qq)) {
                qq = sender.id
            } else {
                return null
            }
        }
        val file = getFileByQQ(qq) ?: return result("获取qq头像失败")

        val qqImg = HttpUtil.getInputStream(url + qq)

        val imageA = ImageUtil.scaleTo(file, 400, 400)
        val imageB = ImageUtil.scaleTo(qqImg, 75, 75)
        val imageC = transformAvatar(imageB)
        val overlay = ImageUtil.overlay(imageA, imageC, 5, 320)
        group.sendImage(overlay)
        return null
    }

    private fun getFileByQQ(qq: Long): File? {
        if (permissionProperties.admin.isNotEmpty() && permissionProperties.admin.contains(qq)) {
            val files = File(tieDir).listFiles()
            if (files == null || files.isEmpty()) return null
            val randomInt = RandomUtil.randomInt(files.size)
            return files[randomInt]
        } else {
            val files = File(paDir).listFiles()
            if (files == null || files.isEmpty()) return null
            val randomInt = RandomUtil.randomInt(files.size)
            return files[randomInt]
        }
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