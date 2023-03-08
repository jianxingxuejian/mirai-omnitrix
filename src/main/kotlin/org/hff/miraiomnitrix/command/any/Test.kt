package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.LightApp
import net.mamoe.mirai.message.data.toMessageChain
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.CommandResult
import org.hff.miraiomnitrix.command.result

@Command(name = ["测试", "test"])
class Test : AnyCommand {

    val jsonString = """
    {
        "app": "com.tencent.structmsg",
        "config": {
            "ctime":1673964813,
            "forward":true,
            "token":"f01705ad86f98cf538ea50856a3d36df",
            "type":"normal"
        },
        "desc":"新闻",
        "meta":{
            "messages":{
                "action":"",
                "android_pkg_name":"",
                "app_type":1,
                "appid":100446242,
                "ctime":1673963424,
                "desc":"dhbf: [图片]\ndhbf: [图片]\n叶:太涩了，举办了！",
                "jumpUrl":"https:\/\/vdse.bdstatic.com\/192d9a98d782d9c74c96f09db9378d93.mp4",
                "preview":"",
                "source_icon":"",
                "source_url":"",
                "tag":"查看8条转发消息",
                "title":"群聊的聊天记录",
                "uin":0
            }
        },
        "prompt":"[聊天记录]",
        "ver":"0.0.0.1",
        "view":"messages"
    }
"""

    override suspend fun execute(args: List<String>, event: MessageEvent): CommandResult? {
        val lightApp = LightApp(jsonString)
        return result(lightApp.toMessageChain())
    }
}