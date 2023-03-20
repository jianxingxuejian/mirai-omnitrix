package org.hff.miraiomnitrix.event.group

import net.mamoe.mirai.event.events.GroupMessageEvent
import org.hff.miraiomnitrix.config.BotProperties
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.event.*
import org.hff.miraiomnitrix.utils.JsonUtil
import org.springframework.core.io.ClassPathResource

/** 二次元回复，词库出自: https://github.com/Kyomotoi/AnimeThesaurus */
@Event(priority = 1)
class Thesaurus(private val permissionProperties: PermissionProperties, private val botProperties: BotProperties) :
    GroupEvent {

    private val thesaurus = hashMapOf<String, MutableSet<String>>()

    init {
        listOf("json/thesaurus1.json", "json/thesaurus1.json", "json/thesaurus3.json").forEach {
            val json = ClassPathResource(it).inputStream.bufferedReader().use { reader -> reader.readText() }
            JsonUtil.fromJson<Map<String, List<String>>>(json).forEach { (key, value) ->
                thesaurus.getOrPut(key) { mutableSetOf() }.addAll(value)
            }
        }
    }

    override suspend fun handle(args: List<String>, event: GroupMessageEvent, isAt: Boolean): EventResult {
        if (!isAt) return next()
        if (permissionProperties.replyExcludeGroup.contains(event.group.id)) return next()
        val arg = args.getOrNull(0) ?: return next()

        when (val list = thesaurus[arg]) {
            null -> {
                if (botProperties.hello != null && (arg.startsWith("你是谁") || arg.startsWith("你好")))
                    return stop(botProperties.hello)
            }

            else -> return list.random().replace("我", "爱丽丝").let(::stop)
        }
        return next()
    }
}
