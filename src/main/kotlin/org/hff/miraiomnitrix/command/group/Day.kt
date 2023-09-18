package org.hff.miraiomnitrix.command.group

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Message
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.GroupCommand
import org.hff.miraiomnitrix.utils.uploadImage
import org.springframework.core.io.ClassPathResource
import java.time.LocalDate

@Command(name = ["今天周几", "今天周几?", "今天周几？"])
class Day : GroupCommand {

    override val needHead = false

    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        val day = LocalDate.now().dayOfWeek.value
        return uploadImage(ClassPathResource("/img/other/day/$day.jpg").inputStream)
    }

}
