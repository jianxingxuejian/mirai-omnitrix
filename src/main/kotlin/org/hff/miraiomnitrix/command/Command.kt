package org.hff.miraiomnitrix.command

interface Command {
    /** 指令名称 */
    val name: Array<String>?
    /** 是否需要指令头或者@机器人，默认是 */
    val isNeedHeader: Boolean
        get() = true
}