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
import org.hff.miraiomnitrix.utils.ImageUtil
import org.hff.miraiomnitrix.utils.ImageUtil.toImmutableImage
import org.hff.miraiomnitrix.utils.ImageUtil.toStream
import org.hff.miraiomnitrix.utils.download
import org.hff.miraiomnitrix.utils.getQqAndRemove
import org.hff.miraiomnitrix.utils.uploadImage
import java.awt.Color
import java.awt.Font

@Command(name = ["游戏王", "ygocard", "ygo"])
class YgoCard : AnyCommand {

    override suspend fun MessageEvent.execute(args: List<String>): Message? {
        val (qq, args1) = args.getQqAndRemove()
        val member =
            if (subject is Group && qq != null) (subject as Group).getMember(qq) ?: return "未查到群成员".toPlainText()
            else sender
        var lb = false
        var type = Type.Spell
        var attr = Attr.Spell
        var name: String? = null
        var content: String? = null
        val args2 = mutableListOf<String>()
        args1.forEach {
            when (it) {
                "暗", "dark" -> attr = Attr.Dark
                "神", "divine" -> attr = Attr.Divine
                "地", "earth" -> attr = Attr.Earth
                "火", "fire" -> attr = Attr.Fire
                "光", "light" -> attr = Attr.Light
                "水", "water" -> attr = Attr.Water
                "风", "wind" -> attr = Attr.Wind
                "魔法" -> {
                    type = Type.Spell
                    attr = Attr.Spell
                }

                "陷阱" -> {
                    type = Type.Trap
                    attr = Attr.Trap
                }

                "灵摆", "lb" -> lb = true

                else -> args2.add(it)
            }
        }
        args2.forEach {
            when (it) {
                "cl", "超量" -> type = if (lb) Type.Monster_cl_lb else Type.Monster_cl
                "lj", "链接" -> type = Type.Monster_lj
                "rh", "融合" -> type = if (lb) Type.Monster_rh_lb else Type.Monster_rh
                "tc", "通常" -> type = if (lb) Type.Monster_tc_lb else Type.Monster_tc
                "tk", "token", "衍生物" -> type = Type.Monster_tk
                "tt", "同调" -> type = if (lb) Type.Monster_tt_lb else Type.Monster_tt
                "xg", "效果" -> type = if (lb) Type.Monster_xg_lb else Type.Monster_xg
                "ys", "仪式" -> type = if (lb) Type.Monster_ys_lb else Type.Monster_ys
                else -> {
                    if (name == null) name = it
                    else content = it
                }
            }
        }
        val nameText = Text(name ?: member.nameCardOrNick, 70, 125) {
            it.color = Color.BLACK
            it.font = Font("SimSun", Font.BOLD, 80)
        }
        val contentText = Text(content ?: "效果未知", 60, 925) {
            it.color = Color.BLACK
            it.font = Font("SimSun", Font.BOLD, 40)
        }
        member.id.download().use { avatar ->
            return Canvas(type.image)
                .draw(nameText).draw(contentText).image
                .overlay(attr.image, 680, 58)
                .overlay(
                    avatar.toImmutableImage(if (lb) 700 else 613, if (lb) 522 else 613),
                    if (lb) 57 else 101,
                    if (lb) 215 else 220
                ).toStream().let { uploadImage(it) }
        }
    }

    enum class Type(val image: ImmutableImage) {
        Spell(ImageUtil.load("/img/ygo/type/spell.jpg")),
        Trap(ImageUtil.load("/img/ygo/type/trap.jpg")),
        Monster_cl(ImageUtil.load("/img/ygo/type/monster_cl.jpg")),
        Monster_cl_lb(ImageUtil.load("/img/ygo/type/monster_cl_lb.jpg")),
        Monster_lj(ImageUtil.load("/img/ygo/type/monster_lj.jpg")),
        Monster_rh(ImageUtil.load("/img/ygo/type/monster_rh.jpg")),
        Monster_rh_lb(ImageUtil.load("/img/ygo/type/monster_rh_lb.jpg")),
        Monster_tc(ImageUtil.load("/img/ygo/type/monster_tc.jpg")),
        Monster_tc_lb(ImageUtil.load("/img/ygo/type/monster_tc_lb.jpg")),
        Monster_tk(ImageUtil.load("/img/ygo/type/monster_tk.jpg")),
        Monster_tt(ImageUtil.load("/img/ygo/type/monster_tt.jpg")),
        Monster_tt_lb(ImageUtil.load("/img/ygo/type/monster_tt_lb.jpg")),
        Monster_xg(ImageUtil.load("/img/ygo/type/monster_xg.jpg")),
        Monster_xg_lb(ImageUtil.load("/img/ygo/type/monster_xg_lb.jpg")),
        Monster_ys(ImageUtil.load("/img/ygo/type/monster_ys.jpg")),
        Monster_ys_lb(ImageUtil.load("/img/ygo/type/monster_ys_lb.jpg")),
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
