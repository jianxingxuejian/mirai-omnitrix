package org.hff.miraiomnitrix.event.group

import net.mamoe.mirai.event.events.GroupMessageEvent
import org.hff.miraiomnitrix.event.Event
import org.hff.miraiomnitrix.event.EventResult
import org.hff.miraiomnitrix.event.GroupEvent

@Event(priority = 6)
class Coin: GroupEvent{

    override suspend fun handle(args: List<String>, event: GroupMessageEvent, isAt: Boolean): EventResult {
        TODO("Not yet implemented")
    }



}
