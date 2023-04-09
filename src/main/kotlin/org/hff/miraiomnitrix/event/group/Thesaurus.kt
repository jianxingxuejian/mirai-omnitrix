package org.hff.miraiomnitrix.event.group

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import org.hff.miraiomnitrix.config.BotProperties
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.event.*
import org.hff.miraiomnitrix.utils.JsonUtil
import org.springframework.core.io.ClassPathResource

/** 二次元回复，词库出自: https://github.com/Kyomotoi/AnimeThesaurus */
@Event(priority = 2)
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

    override suspend fun GroupMessageEvent.handle(args: List<String>, isAt: Boolean): EventResult {
        if (!isAt) return next()
        if (permissionProperties.replyExcludeGroup.contains(group.id)) return next()
        val arg = args.getOrNull(0) ?: return next()

        when (val list = thesaurus[arg]) {
            null -> if ((arg.startsWith("你是谁") && botProperties.hello != null)) return stop(botProperties.hello)
            else -> botProperties.name?.let { return stop(At(sender) + list.random().replace("我", it)) }
        }
        return next()
    }

}
