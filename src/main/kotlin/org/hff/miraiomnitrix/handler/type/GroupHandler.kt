package org.hff.miraiomnitrix.handler.type

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.MessageChain

/** 群消息处理 */
interface GroupHandler {

    /**
     * @param message
     * @param group
     * @return 是否退出消息处理
     */
    suspend fun handle(message: MessageChain, group: Group): Boolean

}