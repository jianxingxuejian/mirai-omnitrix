package org.hff.miraiomnitrix.result

import net.mamoe.mirai.message.data.MessageChain

data class CommandResult(val msg: String?, val msgChain: MessageChain?) {
    companion object {
        fun ok() = CommandResult("执行成功", null)

        fun fail() = CommandResult("执行失败", null)

        fun result(msg: String) = CommandResult(msg, null)

        fun result(message: MessageChain) = CommandResult(null, message)
    }

}