package org.hff.miraiomnitrix.config

import net.mamoe.mirai.utils.BotConfiguration
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
) {

    fun getProtocol() = when (protocol.lowercase()) {
        "mac" -> BotConfiguration.MiraiProtocol.MACOS
        "macos" -> BotConfiguration.MiraiProtocol.MACOS
        "ipad" -> BotConfiguration.MiraiProtocol.IPAD
        "android" -> BotConfiguration.MiraiProtocol.ANDROID_PHONE
        "phone" -> BotConfiguration.MiraiProtocol.ANDROID_PHONE
        "android_phone" -> BotConfiguration.MiraiProtocol.ANDROID_PHONE
        "pad" -> BotConfiguration.MiraiProtocol.ANDROID_PAD
        "android_pad" -> BotConfiguration.MiraiProtocol.ANDROID_PAD
        "watch" -> BotConfiguration.MiraiProtocol.ANDROID_WATCH
        "android_watch" -> BotConfiguration.MiraiProtocol.ANDROID_WATCH
        else -> BotConfiguration.MiraiProtocol.MACOS
    }

}
