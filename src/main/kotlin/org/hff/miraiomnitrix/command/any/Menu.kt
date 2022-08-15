package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.buildMessageChain
import org.hff.miraiomnitrix.command.Command

@Command(name = ["menu", "help", "菜单", "帮助"])
class Menu : AnyCommand {

    override suspend fun execute(
        sender: User,
        message: MessageChain,
        subject: Contact,
        args: List<String>
    ): MessageChain {
        val result = buildMessageChain {
            +"帮助说明(指令以中英文逗号或者@机器人触发)：\n"
            +"1: 随机涩图，关键词(涩图、setu)，可追加参数:n(数量) r(r18) 任意标签，空格或者'|'分隔\n"
            +"2: 壁纸，关键词(壁纸、bizhi)，可追加参数:精选、横屏(pc)、竖屏(mp)、银发、兽耳、涩图、白丝\n"
            +"3: bgm番剧推荐，关键词(bgm)，可追加参数 4位数字年份 r(最低排名) n(数量) 关键词\n"
        }
        return result
    }

}