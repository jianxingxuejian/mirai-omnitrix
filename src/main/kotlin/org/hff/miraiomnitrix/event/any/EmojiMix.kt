package org.hff.miraiomnitrix.event.any

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import org.hff.miraiomnitrix.event.Event
import org.hff.miraiomnitrix.result.EventResult
import org.hff.miraiomnitrix.result.EventResult.Companion.next
import org.hff.miraiomnitrix.result.EventResult.Companion.stop
import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.JsonUtil
import org.springframework.core.io.ClassPathResource

/**
 * emoji合成，json数据来源于 https://github.com/xsalazar/emoji-kitchen
 */
@Event(priority = 5)
class EmojiMix : AnyEvent {

    private val url = "https://www.gstatic.com/android/keyboard/emojikitchen"

    private val emojiCache = hashMapOf<String, HashMap<String, String>>()


    init {
        val json = ClassPathResource("json/emojiData.json").inputStream.readAllBytes().toString(Charsets.UTF_8)
        val emojiMap: Map<String, List<Emoji>> = JsonUtil.fromJson(json)
        emojiMap.values.forEach {
            it.forEach { emoji ->
                val (leftEmoji, rightEmoji, date) = emoji
                val cache = emojiCache[leftEmoji]
                if (cache != null) {
                    cache[rightEmoji] = date
                } else {
                    emojiCache[leftEmoji] = hashMapOf(rightEmoji to date)
                }
            }
        }
    }

    override suspend fun handle(
        sender: User,
        message: MessageChain,
        subject: Contact,
        args: List<String>,
        event: MessageEvent
    ): EventResult {
        if (args.isEmpty()) next()
        val content = args[0]
        val regex = Regex("^(\\p{So}){2}") // 匹配以两个emoji开头的字符串

        val emojis = regex.find(content)?.value ?: return next()
        val emojiCodePoints = emojis.codePoints().toArray()
        val emojiHexStrings = emojiCodePoints.map { Integer.toHexString(it).removePrefix("U+") }
        val first = emojiHexStrings[0]
        val second = emojiHexStrings[1]
        val cache = emojiCache[first] ?: emojiCache[second] ?: return next()
        val data = cache[second] ?: return next()
        val inputStream = HttpUtil.getInputStream("$url/$data/u$first/u${first}_u$second.png")
        subject.sendImage(inputStream)
        return stop()
    }

    data class Emoji(
        val leftEmoji: String,
        val rightEmoji: String,
        val date: String
    )
}
