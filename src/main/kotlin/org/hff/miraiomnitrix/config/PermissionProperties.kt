package org.hff.miraiomnitrix.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("permission")
data class PermissionProperties(
    /** 管理员qq号 */
    val admin: HashSet<Long>,
    /** 排除复读功能的群号 */
    val repeatExcludeGroup: HashSet<Long>,
    /** 排除聊天功能的群号 */
    val chatExcludeGroup: HashSet<Long>,
    /** 排除bv解析的群号 */
    val bvExcludeGroup: HashSet<Long>,
    /** 排除合成emoji的群号 */
    val emojiMixExcludeGroup: HashSet<Long>,
    /** 排除回复功能的群号 */
    val replyExcludeGroup: HashSet<Long>,
    /** 不需要指令头就可以聊天的群号(会影响部分功能) */
    val chatPlusNotNeedCommandGroup: HashSet<Long>,
    /** 开启挖矿功能的群 */
    val mineIncludeGroup: HashSet<Long>,
    /** 排除github链接解析的群号 */
    val githubExcludeGroup: HashSet<Long>,
)
