package org.hff.miraiomnitrix.command.group

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Message
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.GroupCommand

@Command(name = ["关键词", "违禁词", "keyword"])
class Keyword : GroupCommand {
    override suspend fun execute(args: List<String>, event: GroupMessageEvent): Message? {

//        if (args.isEmpty()) return result("请使用add、del命令添加或者删除关键词")
//        if(args.size<2) return result("参数错误")
//        when(args[0]){
//            "add","添加"->{
//
//            }
//        }
        return null
    }
}
