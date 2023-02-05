package org.hff.miraiomnitrix.result

import net.mamoe.mirai.message.data.MessageChain

data class EventResult(val stop: Boolean, val msg: String?, val msgChain: MessageChain?) {

    companion object {
        fun stop() = result(true)

        fun stop(msg: String?) = result(true, msg)

        fun stop(msgChain: MessageChain?) = result(true, msgChain)

        fun next() = result(false)

        fun next(msg: String?) = result(false, msg)

        fun next(msgChain: MessageChain?) = result(false, msgChain)

        fun result(stop: Boolean) = EventResult(stop, null, null)

        fun result(stop: Boolean, msg: String?) = EventResult(stop, msg, null)

        fun result(stop: Boolean, msgChain: MessageChain?) = EventResult(stop, null, msgChain)

    }
}