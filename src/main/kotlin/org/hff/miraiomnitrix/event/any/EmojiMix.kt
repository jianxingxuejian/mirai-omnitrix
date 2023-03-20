package org.hff.miraiomnitrix.event.any

import net.mamoe.mirai.event.events.MessageEvent
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.event.*
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.JsonUtil
import org.hff.miraiomnitrix.utils.sendImageAndCache
import org.springframework.core.io.ClassPathResource

/** emoji合成，json数据来源于 https://github.com/xsalazar/emoji-kitchen */
@Event(priority = 1)
class EmojiMix(private val permissionProperties: PermissionProperties) : AnyEvent {

    private val url = "https://www.gstatic.com/android/keyboard/emojikitchen"

    private val emojiCache = hashMapOf<String, HashMap<String, Emoji>>()


    init {
        val json =
            ClassPathResource("json/emojiData.json").inputStream.use { it.readAllBytes().toString(Charsets.UTF_8) }
        val emojiMap: Map<String, List<Emoji>> = JsonUtil.fromJson(json)
        emojiMap.values.forEach {
            it.forEach { emoji ->
                val (leftEmoji, rightEmoji) = emoji
                val cache = emojiCache[leftEmoji]
                if (cache != null) {
                    cache[rightEmoji] = emoji
                } else {
                    emojiCache[leftEmoji] = hashMapOf(rightEmoji to emoji)
                }
            }
        }
    }

    override suspend fun handle(args: List<String>, event: MessageEvent, isAt: Boolean): EventResult {
        if (args.isEmpty()) return next()
        val subject = event.subject
        if (permissionProperties.emojiMixExcludeGroup.contains(subject.id)) return next()

        val regex = Regex("^(\\p{So}){2}") // 匹配以两个emoji开头的字符串
        val emojis = regex.find(args[0])?.value ?: return next()
        val emojiCodePoints = emojis.codePoints().toArray()
        val emojiHexStrings = emojiCodePoints.map { Integer.toHexString(it).removePrefix("U+") }
        val first = emojiHexStrings[0]
        val second = emojiHexStrings[1]
        val (leftEmoji, rightEmoji, date) = emojiCache[first]?.get(second)
            ?: emojiCache[second]?.get(first)
            ?: return next()
        HttpUtil.getInputStream("$url/$date/u$leftEmoji/u${leftEmoji}_u$rightEmoji.png")
            .use { subject.sendImageAndCache(it) }.run { return stop() }
    }

    data class Emoji(
        val leftEmoji: String,
        val rightEmoji: String,
        val date: String
    )
}
