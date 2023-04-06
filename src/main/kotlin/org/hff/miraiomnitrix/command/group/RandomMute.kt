package org.hff.miraiomnitrix.command.group

import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.GroupCommand
import org.hff.miraiomnitrix.utils.getQq

@Command(name = ["随机禁言", "禁言", "mute"])
class RandomMute : GroupCommand {

    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val bot = group.botAsMember
        if (bot.permission == MemberPermission.MEMBER) return "没有管理员权限".toPlainText()
        if (sender.permission.level >= bot.permission.level) return "权限不足".toPlainText()
        val randomNumber = (0..600).random()
        val member = args.getQq()?.let(group::getMember) ?: sender
        member.mute(randomNumber)
        return "${member.nameCardOrNick}被禁言${randomNumber}秒".toPlainText()
    }
}
