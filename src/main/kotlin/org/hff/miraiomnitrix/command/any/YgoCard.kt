package org.hff.miraiomnitrix.command.any

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.canvas.Canvas
import com.sksamuel.scrimage.canvas.drawables.Text
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.utils.*
import org.hff.miraiomnitrix.utils.ImageUtil.toImmutableImage
import org.hff.miraiomnitrix.utils.ImageUtil.toStream
import java.awt.Color
import java.awt.Font

@Command(name = ["游戏王", "ygocard", "ygo"])
class YgoCard : AnyCommand {

    override suspend fun execute(args: List<String>, event: MessageEvent): Message? {
        val (subject, sender) = event.getInfo()
        val qq = args.getQq()
        val member =
            if (subject is Group && qq != null) subject.getMember(qq) ?: return "未查到群成员".toPlainText()
            else sender
        val name = Text(args.getOrNull(1) ?: member.nameCardOrNick, 70, 125) {
            it.color = Color.BLACK
            it.font = Font("SimSun", Font.BOLD, 80)
        }
        val content = Text(args.getOrNull(2) ?: "效果未知", 60, 925) {
            it.color = Color.BLACK
            it.font = Font("SimSun", Font.BOLD, 40)
        }
        sender.id.toAvatar().use { avatar ->
            Canvas(Type.Spell.image)
                .draw(name).draw(content).image
                .overlay(Attr.Spell.image, 680, 58)
                .overlay(avatar.toImmutableImage(613, 613), 101, 220)
                .toStream().use { subject.sendImageAndCache(it) }
        }
        return null
    }

    enum class Type(val image: ImmutableImage) {
        Spell(ImageUtil.load("/img/ygo/type/spell.jpg")),
        Trap(ImageUtil.load("/img/ygo/type/trap.jpg")),
        Monster_lj(ImageUtil.load("/img/ygo/type/monster_lj.jpg")),
        Monster_rh(ImageUtil.load("/img/ygo/type/monster_rh.jpg")),
        Monster_tc(ImageUtil.load("/img/ygo/type/monster_tc.jpg")),
        Monster_tk(ImageUtil.load("/img/ygo/type/monster_tk.jpg")),
        Monster_tt(ImageUtil.load("/img/ygo/type/monster_tt.jpg")),
        Monster_xg(ImageUtil.load("/img/ygo/type/monster_xg.jpg")),
        Monster_ys(ImageUtil.load("/img/ygo/type/monster_ys.jpg"));
    }

    enum class Attr(val image: ImmutableImage) {
        Dark(ImageUtil.load("/img/ygo/attr/dark.png", 72, 72)),
        Divine(ImageUtil.load("/img/ygo/attr/divine.png")),
        Earth(ImageUtil.load("/img/ygo/attr/earth.png")),
        Fire(ImageUtil.load("/img/ygo/attr/fire.png")),
        Light(ImageUtil.load("/img/ygo/attr/light.png")),
        Spell(ImageUtil.load("/img/ygo/attr/spell.png", 75, 75)),
        Trap(ImageUtil.load("/img/ygo/attr/trap.png", 75, 75)),
        Water(ImageUtil.load("/img/ygo/attr/water.png")),
        Wind(ImageUtil.load("/img/ygo/attr/wind.png")),
    }

}
