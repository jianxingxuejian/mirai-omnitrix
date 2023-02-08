package org.hff.miraiomnitrix.command.group

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.result.CommandResult

@Command(name = ["关键词", "违禁词", "keyword"])
class Keyword : GroupCommand {
    override suspend fun execute(
        sender: Member,
        message: MessageChain,
        group: Group,
        args: List<String>,
        event: GroupMessageEvent
    ): CommandResult? {

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