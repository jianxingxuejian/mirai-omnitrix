package org.hff.miraiomnitrix.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("permission")
data class PermissionProperties(
    /** 管理员qq号 */
    val admin: List<Long>,
    /** 排除复读功能的群号 */
    val repeatExcludeGroup: List<Long>,
    /** 排除聊天功能的群号 */
    val chatExcludeGroup: List<Long>,
    /** 排除bv解析的群号 */
    val bvExcludeGroup: List<Long>,
    /** 排除合成emoji的群号 */
    val emojiMixExcludeGroup: List<Long>,
    /** 排除回复功能的群号 */
    val replyExcludeGroup: List<Long>,
    /** 不需要指令头就可以聊天的群号(会影响部分功能) */
    val chatPlusNotNeedCommandGroup: List<Long>
)
