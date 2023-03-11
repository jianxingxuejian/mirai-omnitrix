package org.hff.miraiomnitrix.event.group

import com.google.common.util.concurrent.RateLimiter
import net.mamoe.mirai.event.events.GroupMessageEvent
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.event.*
import org.hff.miraiomnitrix.utils.JsonUtil
import org.springframework.core.io.ClassPathResource

/** 二次元回复，词库出自: https://github.com/Kyomotoi/AnimeThesaurus */
@Event(priority = 1)
class Thesaurus(permissionProperties: PermissionProperties) : GroupEvent {

    val thesaurus = hashMapOf<String, MutableSet<String>>()
    private val limiterMap = HashMap<Long, RateLimiter>()

    init {
        parseData("json/thesaurus1.json")
        parseData("json/thesaurus2.json")
        parseData("json/thesaurus3.json")
        permissionProperties.thesaurusIncludeGroup.forEach { limiterMap[it] = RateLimiter.create(10.0 / 60.0) }
    }

    private fun parseData(path: String) {
        val json = ClassPathResource(path).inputStream.readAllBytes().toString(Charsets.UTF_8)
        val map: Map<String, List<String>> = JsonUtil.fromJson(json)
        map.forEach { (key, value) ->
            if (thesaurus[key] == null) thesaurus[key] = mutableSetOf()
            thesaurus[key]!!.addAll(value)
        }
    }

    override suspend fun handle(args: List<String>, event: GroupMessageEvent): EventResult {
        val limiter = limiterMap[event.group.id] ?: return next()
        with(limiter) { if (!tryAcquire()) return next() }
        val arg = args[0]
        if (arg.isBlank()) return next()
        val list = thesaurus[arg] ?: return next()
        val reply = list.elementAt((0 until list.size).random())
        return stop(reply)
    }
}