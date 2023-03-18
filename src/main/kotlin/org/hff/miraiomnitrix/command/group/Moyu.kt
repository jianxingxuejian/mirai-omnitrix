package org.hff.miraiomnitrix.command.group

import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.GroupMessageEvent
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.CommandResult
import org.hff.miraiomnitrix.command.GroupCommand
import org.hff.miraiomnitrix.command.result
import org.hff.miraiomnitrix.utils.HttpUtil
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Command(name = ["moyu", "摸鱼"])
class Moyu : GroupCommand {

    private val url = "https://api.j4u.ink/proxy/redirect/moyu/calendar/"

    override suspend fun execute(args: List<String>, event: GroupMessageEvent): CommandResult? {
        val formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        return HttpUtil.getInputStream("$url$formattedDate.png").use { event.group.uploadImage(it) }.run(::result)
    }
}
