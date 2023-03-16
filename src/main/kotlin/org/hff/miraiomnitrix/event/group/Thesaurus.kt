package org.hff.miraiomnitrix.event.group

import net.mamoe.mirai.event.events.GroupMessageEvent
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.event.*
import org.hff.miraiomnitrix.utils.JsonUtil
import org.springframework.core.io.ClassPathResource

/** 二次元回复，词库出自: https://github.com/Kyomotoi/AnimeThesaurus */
@Event(priority = 1)
class Thesaurus(private val permissionProperties: PermissionProperties) : GroupEvent {

    val thesaurus = hashMapOf<String, MutableSet<String>>()

    init {
        parseData("json/thesaurus1.json")
        parseData("json/thesaurus2.json")
        parseData("json/thesaurus3.json")
    }

    private fun parseData(path: String) {
        val json = ClassPathResource(path).inputStream.readAllBytes().toString(Charsets.UTF_8)
        val map: Map<String, List<String>> = JsonUtil.fromJson(json)
        map.forEach { (key, value) ->
            if (thesaurus[key] == null) thesaurus[key] = mutableSetOf()
            thesaurus[key]!!.addAll(value)
        }
    }

    override suspend fun handle(args: List<String>, event: GroupMessageEvent, isAt: Boolean): EventResult {
        if (!isAt) return next()
        if (permissionProperties.replyExcludeGroup.contains(event.group.id)) return next()
        val arg = args.getOrNull(0) ?: return next()

        when (val list = thesaurus[arg]) {
            null -> {
                if (arg.startsWith("你是谁") && arg.length <= 4)
                    return stop("我是幻书《爱丽丝梦游仙境》，你可以叫我爱丽丝")
                if (arg.startsWith("你好") && arg.length <= 7)
                    return stop("贵安～我是仙境的爱丽丝，你愿意与我一同前往神奇的国度，去寻找真正的乐园吗？")
            }

            else -> list.random().replace("我", "爱丽丝").apply { return stop(this) }
        }
        return next()
    }
}
