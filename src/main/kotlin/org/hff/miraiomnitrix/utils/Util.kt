package org.hff.miraiomnitrix.utils

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private const val QQ_URL = "https://q1.qlogo.cn/g?b=qq&s=640&nk="

fun String.toUrl(): String = URLEncoder.encode(this, StandardCharsets.UTF_8)

fun String.getQq() = if (this.startsWith("@"))
    this.substring(1).toLongOrNull()
else
    this.toLongOrNull()

fun List<String>.getQq(): Long? {
    for (arg in this) {
        val qq = arg.getQq()
        if (qq != null) {
            return qq
        }
    }
    return null
}

fun Long.toAvatar() = HttpUtil.getInputStream(QQ_URL + this)




