package org.hff.miraiomnitrix.result

import net.mamoe.mirai.message.data.MessageChain

data class ResultMessage(val msg: String?, val msgChain: MessageChain?)

fun ok() = ResultMessage("操作成功", null)

fun fail() = ResultMessage("网络错误", null)

fun result(msg: String) = ResultMessage(msg, null)

fun result(message: MessageChain) = ResultMessage(null, message)

fun result(msg: String, message: MessageChain) = ResultMessage(msg, message)