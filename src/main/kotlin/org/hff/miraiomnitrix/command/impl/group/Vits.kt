package org.hff.miraiomnitrix.command.impl.group

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.hff.miraiomnitrix.command.core.Command
import org.hff.miraiomnitrix.command.type.GroupCommand
import org.hff.miraiomnitrix.result.ResultMessage
import org.hff.miraiomnitrix.result.fail
import org.hff.miraiomnitrix.result.result
import org.hff.miraiomnitrix.utils.HttpUtil

@Command(name = ["语音", "vits", "yuyin"])
class Vits : GroupCommand {

    private val url = "http://233366.proxy.nscc-gz.cn:8888/?"

    private val avatars = arrayOf(
        "派蒙", "凯亚", "安柏", "丽莎", "琴", "香菱", "枫原万叶", "迪卢克", "温迪", "可莉", "早柚", "托马", "芭芭拉", "优菈", "云堇",
        "钟离", "魈", "凝光", "雷电将军", "北斗", "甘雨", "七七", "刻晴", "神里绫华", "戴因斯雷布", "雷泽", "神里绫人", "罗莎莉亚",
        "阿贝多", "八重神子", "宵宫", "荒泷一斗", "九条裟罗", "夜兰", "珊瑚宫心海", "五郎", "散兵", "女士", "达达利亚", "莫娜", "班尼特",
        "申鹤", "行秋", "烟绯", "久岐忍", "辛焱", "砂糖", "胡桃", "重云", "菲谢尔", "诺艾尔", "迪奥娜", "鹿野院平藏"
    )

    override suspend fun execute(
        sender: Member,
        message: MessageChain,
        group: Group,
        args: List<String>
    ): ResultMessage? {
        if (args.size < 2) return result("参数错误")
        val speaker = args[0]
        if (!avatars.contains(speaker)) return result("角色名错误")
        val text = args[1]
        val response = HttpUtil.getInputStream(url + "speaker=" + speaker + "&text=" + text)
        if (response.statusCode() != 200) return fail()
        val audio = response.body().toExternalResource().use { group.uploadAudio(it) }
        group.sendMessage(audio)
        return null
    }
}