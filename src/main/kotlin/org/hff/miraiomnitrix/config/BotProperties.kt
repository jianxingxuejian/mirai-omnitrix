package org.hff.miraiomnitrix.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("bot")
data class BotProperties(
    /** qq号 */
    val qq: Long?,
    /** qq密码 */
    val password: String?,
    /** 协议类型 */
    val protocol: String = "mac",
    /** bot名字 */
    val name: String?,
    /** 招呼语 */
    val hello: String?,
)
