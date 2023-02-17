package org.hff.miraiomnitrix.command.any

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChainBuilder
import org.hff.miraiomnitrix.command.*
import org.hff.miraiomnitrix.config.AccountProperties
import org.hff.miraiomnitrix.db.entity.Bgm
import org.hff.miraiomnitrix.db.service.BgmService
import org.hff.miraiomnitrix.utils.HttpUtil
import org.jsoup.Jsoup
import java.util.regex.Pattern

@Command(name = ["番剧推荐", "bgm"])
class Bgm(private val bgmService: BgmService, private val accountProperties: AccountProperties) : AnyCommand {

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun execute(args: List<String>, event: MessageEvent): CommandResult? {
        if (args.size == 1 && (args[0] == "init" || args[0] == "初始化")) return init()

        var num: Short = 1
        val wrapper = bgmService.ktQuery()
        args.forEach {
            when {
                it.matches(Regex("^[0-9]{4}$")) -> wrapper.eq(Bgm::year, it.toShort())
                it.matches(Regex("^r[0-9]{1,4}$")) -> wrapper.le(Bgm::rank, it.substring(1).toShort())
                it.matches(Regex("^n[0-9]{1,2}$")) -> num = it.substring(1).toShort()
                else -> wrapper.like(Bgm::name, "%$it%")
            }
        }
        val list = wrapper.last("ORDER BY RANDOM() LIMIT $num").list()

        val subject = event.subject
        list.forEach {
            GlobalScope.launch {
                val msg = MessageChainBuilder()
                if (it.imgUrl != null && !it.imgUrl.equals("https:/img/no_icon_subject.png")) {
                    val result = HttpUtil.getInputStreamByProxy(it.imgUrl!!)
                    val image = subject.uploadImage(result)
                    msg.append(image)
                }
                msg.append("名字: ${it.name}\n")
                    .append("年份: ${it.year}\n")
                    .append("排名: ${it.rank}\n")
                    .append("评分: ${it.rate}(${it.rateNum}人)\n")
                    .append("说明: ${it.info}\n")
                subject.sendMessage(msg.build())
            }
        }

        return null
    }

    fun init(): CommandResult? {
        val cookie = accountProperties.bgmCookie
        if (cookie.isNullOrBlank()) return result("请配置bangumi网页的cookie")
        val headers = mapOf("cookie" to cookie)
        for (i in 1..300) {
            val result = HttpUtil.getString("https://bgm.tv/anime/browser?sort=rank&page=$i", headers)
            val document = Jsoup.parse(result)
            val list = document.select("#browserItemList")
            list.first()?.children()?.forEach { item ->
                val bgm = Bgm()
                bgm.imgUrl = "https:" + item.select(".image")[0].child(0).attr("src")
                bgm.name = item.select("h3 a").text()
                if (item.select("h3 small").size > 0) {
                    bgm.nameOriginal = item.select("h3 small")[0].text()
                }
                bgm.rank = item.select(".rank").text().substring(5).toShort()
                bgm.info = item.child(1).child(2).text()
                bgm.rate = item.child(1).child(3).child(1).text().toBigDecimal()
                val text = item.child(1).child(3).child(2).text()
                bgm.rateNum = Pattern.compile("[^0-9]").matcher(text).replaceAll("").toShort()
                val regex = "[0-9]{4}年"
                val matcher = bgm.info?.let { Pattern.compile(regex).matcher(it) }
                if (matcher?.find() == true) {
                    bgm.year = matcher.group().substring(0, 4).toShort()
                } else {
                    bgm.year = 0
                }
                bgmService.ktQuery().eq(Bgm::year, 0).list()
                bgmService.save(bgm)
            }
        }
        return null
    }
}