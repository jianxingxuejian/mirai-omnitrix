package org.hff.miraiomnitrix.utils

import net.mamoe.mirai.contact.User
import org.hff.miraiomnitrix.BotRunner
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object Util {
    private const val QQ_URL = "https://q1.qlogo.cn/g?b=qq&s=640&nk="

    fun atBot() = "@" + BotRunner.bot.id

    fun getQq(args: List<String>) = args.find { it.startsWith("@") }?.substring(1)?.toLong()

    fun getQq(args: List<String>, sender: User) = getQq(args) ?: sender.id

    fun getQq(arg: String) = if (arg.startsWith("@")) arg.substring(1).toLong() else arg.toLong()

    fun getQqImg(qq: Long) = HttpUtil.getInputStream(QQ_URL + qq)

    fun getQqImg(args: List<String>, sender: User) = HttpUtil.getInputStream(QQ_URL + getQq(args, sender))
}

fun <T> Collection<T>.containsAny(collection: Collection<T>): Boolean {
    return this.any { it in collection }
}

fun String.toUrl(): String = URLEncoder.encode(this, StandardCharsets.UTF_8)




