package org.hff.miraiomnitrix.command.group

import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.events.GroupMessageEvent
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.CommandResult
import org.hff.miraiomnitrix.command.GroupCommand
import org.hff.miraiomnitrix.command.result
import org.hff.miraiomnitrix.utils.getInfo

@Command(name = ["随机禁言", "禁言", "mute"])
class RandomMute : GroupCommand {

    override suspend fun execute(args: List<String>, event: GroupMessageEvent): CommandResult? {
        val (group, sender) = event.getInfo()
        val bot = group.botAsMember
        if (bot.permission == MemberPermission.MEMBER) return result("没有管理员权限")
        if (sender.permission.level >= bot.permission.level) return result("权限不足")
        val randomNumber = (0..600).random()
        sender.mute(randomNumber)
        return null
    }
}
