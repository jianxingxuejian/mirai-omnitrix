package org.hff.miraiomnitrix.event.group

import net.mamoe.mirai.event.events.GroupMessageEvent
import org.hff.miraiomnitrix.event.Event
import org.hff.miraiomnitrix.event.EventResult
import org.hff.miraiomnitrix.event.GroupEvent
import org.hff.miraiomnitrix.event.stop

// TODO: 挖矿
@Event(priority = 6)
class Mine : GroupEvent {

    override suspend fun GroupMessageEvent.handle(args: List<String>, isAt: Boolean): EventResult {
        return stop()
    }

}
