package org.hff.miraiomnitrix.utils

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private const val QQ_URL = "https://q1.qlogo.cn/g?b=qq&s=640&nk="

fun String.getQq() = if (startsWith("@")) substring(1).toLongOrNull() else toLongOrNull()

fun List<String>.getQq(): Long? {
    for (arg in this) {
        arg.getQq()?.run { return this }
    }
    return null
}

fun List<String>.getQqAndRemove(): Pair<Long?, List<String>> {
    val newStringList = toMutableList()
    for (arg in this) {
        arg.getQq()?.run {
            newStringList.remove(arg)
            return Pair(this, newStringList)
        }
    }
    return Pair(null, this)
}

/** 转义字符串 */
fun String.toUrl(): String = URLEncoder.encode(this, StandardCharsets.UTF_8)

/** 根据qq下载头像 */
suspend fun Long.download() = HttpUtil.getInputStream(QQ_URL + this)

/**
 * forEach的launch版本
 *
 * @see forEach
 */
suspend inline fun <T> Iterable<T>.forEachLaunch(crossinline action: suspend (T) -> Unit) =
    coroutineScope {
        forEach { launch { action(it) } }
    }

/** 秒数转成时间字符串 */
fun Int.toTime(): String {
    val day = this / 86400
    val hours = this / 3600
    val minutes = this % 3600 / 60
    val seconds = this % 60
    return when {
        day > 0 -> String.format("%d天%02d:%02d:%02d", day, hours, minutes, seconds)
        hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
        minutes > 0 -> String.format("%02d:%02d", minutes, seconds)
        else -> String.format("%02d", seconds)
    }
}

/** @see toTime */
fun Double.toTime() = toInt().toTime()
fun Long.toTime() = (this / 1000).toInt().toTime()
