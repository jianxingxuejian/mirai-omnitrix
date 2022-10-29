package org.hff.miraiomnitrix

import kotlinx.coroutines.runBlocking
import org.hff.miraiomnitrix.app.entity.Bgm
import org.hff.miraiomnitrix.app.service.BgmService
import org.hff.miraiomnitrix.utils.HttpUtil
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.regex.Pattern

@SpringBootTest
class MiraiOmnitrixApplicationTests {

    val cookie =
        "chii_sec_id=4gijhLTbY6y4aSakvpBF7LGTlJ5M1PFcUFowiEs; chii_theme=light; __utmc=1; chii_cookietime=2592000; __utmz=1.1657900311.4.2.utmcsr=baidu|utmccn=(organic)|utmcmd=organic; chii_sid=1x12xA; __utma=1.1981272127.1652013383.1657900311.1660389494.5; chii_auth=GVi9lslg2cqpu10JEyMAj64pm2TL0UN6DQIwkOT67VPiHTUZeTV8yP0%2B%2BI9tXNWEnXmHxvfCjGGERY5tpdnDOn7MNuVI5j8yQpoz; __utmb=1.5.10.1660389494"

    @Autowired
    private lateinit var bgmService: BgmService

    @Test
    fun test() {
        runBlocking {
            for (i in 151..300) {
                val response = HttpUtil.getString("https://bgm.tv/anime/browser?sort=rank&page=$i", null, cookie)
                if (response?.statusCode() != 200) return@runBlocking
                val document = Jsoup.parse(response.body())
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
        }
    }

//    @Test
//    fun contextLoads() {
//    }

}
