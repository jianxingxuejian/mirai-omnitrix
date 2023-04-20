package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.uploadImage

@Command(name = ["news", "60s", "60秒"])
class News : AnyCommand {

    override suspend fun MessageEvent.execute(args: List<String>): Message? =
        uploadImage(HttpUtil.getInputStream("https://api.03c3.cn/zb"))

}
