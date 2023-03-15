package org.hff.miraiomnitrix

import org.hff.miraiomnitrix.utils.HttpUtil
import org.hff.miraiomnitrix.utils.JsonUtil
import org.hff.miraiomnitrix.utils.getAsArray
import org.hff.miraiomnitrix.utils.getAsStr
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.io.File

@SpringBootTest
class MiraiOmnitrixApplicationTests {

    @Test
    fun contextLoads() {
    }

    @Test
    fun downloadGenshinAvatar() {
        val json =
            HttpUtil.getString("https://api-static.mihoyo.com/common/blackboard/ys_obc/v1/home/content/list?app_sn=ys_obc&channel_id=189")
        JsonUtil.getObj(json, "data").getAsArray("list")[0].getAsArray("children")[0].getAsArray("list").forEach {
            val icon = it.getAsStr("icon")
            val title = it.getAsStr("title")
            write(icon, "src/main/resources/img/genshin/avatar/$title.png")
        }
    }

    @Test
    fun downloadBlhxAvatar() {
        val html = HttpUtil.getString("https://wiki.biligame.com/blhx/%E8%88%B0%E8%88%B9%E5%9B%BE%E9%89%B4")
        val document = Jsoup.parse(html).getElementById("CardSelectTr") ?: return
        document.children().forEach {
            val src = it.child(0).child(0).attr("src")
            val title = it.child(1).child(0).attr("title")
            write(src, "src/main/resources/img/blhx/avatar/$title.jpg")
        }
    }

    @Test
    fun downloadArknightsAvatar() {
        val html = HttpUtil.getString("https://wiki.biligame.com/arknights/%E5%B9%B2%E5%91%98")
        val document = Jsoup.parse(html).getElementsByClass("mw-parser-output")[0].children()
        (1..10).forEach { i ->
            val num = 5 + i * 4
            document[num].child(0).children().forEach {
                val item = it.child(0).child(0).child(0)
                val src = item.attr("src")
                val title = item.attr("alt")
                write(src, "src/main/resources/img/arknights/avatar/$title.jpg")
            }
        }
    }

    @Test
    fun downloadBaAvatar() {
        val headers = mapOf("game-alias" to "ba")
        val json = HttpUtil.getString("https://ba.gamekee.com/v1/wiki/entry", headers)
        val jsonArray = JsonUtil.getObj(json, "data").getAsArray("entry_list")[4].getAsArray("child")
        jsonArray[2].getAsArray("child").forEach {
            val icon = it.getAsStr("icon")
            val name = it.getAsStr("name")
            val path = "src/main/resources/img/ba/avatar/$name.png"
            write("https:$icon", path)
        }
        jsonArray[3].getAsArray("child").forEach {
            val icon = it.getAsStr("icon")
            val name = it.getAsStr("name")
            val path = "src/main/resources/img/ba/avatar/$name.png"
            write("https:$icon", path)
        }
    }

//    const list = document.getElementsByClassName('collection-avatar')[0].children
//    const srcArray = []
//    for (let i = 0; i < list.length; i++) {
//        const src = list[i].children[0].getAttribute('data-src')
//        srcArray.push(src)
//    }
//    console.log(JSON.stringify(srcArray))

    @Test
    fun downloadBh3Avatar() {
        val html =
            HttpUtil.getString("https://bbs.mihoyo.com/bh3/wiki/channel/map/17/18?bbs_presentation_style=no_header")
        val srcList = listOf(
            "https://uploadstatic.mihoyo.com/bh3-wiki/2023/02/20/50494840/0e20a93c735bdd7de8334506bab1c762_2957356932382539974.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2023/01/04/264755623/54085ecaead652d8e84cd1c9907f7b38_5757792017872581659.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2023/01/04/282941837/a1053e4cf2715fd203c1af5fd1e20f81_2299856813111757857.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2022/11/26/50494840/d18a2f8b79062b6b0855a91b61b5b37a_7009407622785809505.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2022/11/02/264755623/924232a8313c69a16bc6f558596a4077_6095047626221230688.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2022/09/09/50494840/a2cac9bd01f70b177a8d6d28af25a919_8274598273580102056.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2022/08/02/264755623/075a2ae601c87cce283ecbaacea8a382_9124843153772421079.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2022/06/25/50494840/5e842f81fb00e8a5e5dcee41de927e80_6987120996411923087.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2022/05/14/6100274/ec019393554be6046729e8cfe917e6b3_8670113222753439293.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2022/04/19/6100274/949ae1803612185b3088083c0c397014_5041467497139282837.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2022/04/20/50494840/e2681f67e08b3fdec0b7daf8bf5415bf_2410624719295384696.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2022/04/20/50494840/9574416af04793aaf5895759c8f15297_4262776646744624118.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2022/01/10/6100274/03bd3f0bf8ad3bc65349fd2f041d5b00_2706168041269995737.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2022/01/07/6100274/44b273c7339ea0823da5b76ccdc17356_7195812762288875735.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/11/26/6100274/e5b6cc6347304d59839271390982ace6_7520428016051104938.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/11/02/77124895/b142b62d1962ab81351d55fce19d6fc7_2365488698118869948.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/11/02/77124895/480aede3b9d5aa95490fa80acfbab92b_8992930320807638919.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/11/02/77124895/99d16c47d57a1a05c9a7c95ed36f44ad_3607419813930398239.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/10/25/50494840/1d45e519b5b61332cd4a57eb6af0d2b4_691189736919064257.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/26/73514954/fd50a3be09cfd8b68ccd1b92fab787c0_673803583445326954.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/77124895/858c9b51dc2c3ff63bb2bac4c0d7d8ce_744875381108854296.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/81417673/b7434607dcc7c80dd970ed4cf682d44a_4066663144614144526.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/75216984/67fc268130bd2257c27494d2222b1a9c_5673311471453759662.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/72350798/d9fa24209b4794ef2ea60b2cf6fe1165_1799979834132859276.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/11/03/6100274/51d9b0cbb7bffdb2959cf9043ca148b6_2778886755677175184.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/03/77124895/ecb5081d94e7958d7adeb2a8c4e9db6a_9007466906099986460.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/76361817/8e458679d4ba1c40f1da5cb80bfb8a32_209160976408038839.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/73514954/38d8af9071da6752018a94f8a06f52a8_3573692414916686224.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/10/6100274/739a20190f3d9061d824d2088f399624_2815573921042532384.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/75216984/7703592f2ccba8dbcb8983f60f204da6_4616011937770777163.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/75216984/a78e92d4c8fd2863c3167497372a4f26_8108971467597823850.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/81417673/7fc38e2c5eaa4bcb92197bdc20949c66_5924939664871217583.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/03/73514954/0e1475d378a3bd6815f2735264397b13_3770607914018558266.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/03/76361817/79feaa091c0f3d90a02ba70921e685f8_5693361591508065282.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/81417673/27c0fac9c8976d6cb60eb665be803950_917401153420187291.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/76361817/84d23452953df9eec64a4341ef46c428_7518514946934375460.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/50494840/e11db5914603dbf3fdd7bb85cfbfde97_1524894216829192288.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/03/77124895/d0ccfe37b35dfd258b93aba683b8a61a_1321844921087016538.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/03/77124895/16ed35bf60178e6d8707d12547e35087_8533105621720987823.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/05/73514954/e066851e9ced3eb012f3935ddb23e9be_1919043134123463670.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/76361817/6d4062cf9a0f86ca665448fafd69ff36_993198924277790187.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/91006211/7720a55053a4ed4299637c6ff368ba58_1135167589200525.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/75216984/c695d129ada34d5821d8b6fb0c24c43f_3739634654550385914.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/73514954/5181b1c63bf80fa4d20c817552267ef5_3724178600812535419.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/03/6100274/bed127bf485e14bcc438e59d4b5e40e3_4185805613378774475.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/05/73514954/d33157f6b657ad08c7b570e1206b6713_8563328491570739940.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/75216984/99c1c77a5aa890706e6fff6d142ca56c_1229405235298316587.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/72350798/471d9cf2266105fd7c6e7ce7778226a8_8051259659252164657.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/72350798/b2cb6f16f2ce7c111c9910413625a5e0_3263177696179191025.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/03/77124895/8ec3337b01c7aea933370b5171db6b02_5203449406442274314.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/72350798/9ee138ba90740c662dc3aada9849f0a7_657266813034621058.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/50494840/e4eb2c6e750999cf18eb16785f166c60_5109839785243234098.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/91006211/207c1f230e13cac3e088d102345c389a_7231313932039159273.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/72350798/13eb7de63a05f63d591c236efaaf4d7d_7095228264818314213.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/72350798/a7a35d53a3faa6865f7db2a93125be0d_270901143305220082.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/05/73514954/e2c7322cd78d40d558bda4683e4e3a85_4219207119064686877.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/91006211/2f43a01086baf2f31b1125344aea399a_7819039452978395141.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/72350798/b50c180c92aab1de400c3d86d159a75f_1106909501462214359.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/03/91006211/83693d05e2aeddf5ab3878675d4d5084_7958262682707723488.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/03/81417673/9ba29183b4911598b3653501da43a456_6793604440780782760.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/81417673/dd22aeb7b6a1f41769069d4248919bf8_4033008633210264989.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/50494840/2829e1569f882deb968bf74281498d59_8011782976755008978.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/50494840/17c2874ad793555f997f85e418936868_977688081728654796.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/76361817/b9c0cd9d69031497650b05e92ffb48fe_8677499444287713630.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/76361817/6084af2881614fae5cb2877a7d0b1a89_7623788348504630627.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/76361817/2fd798c004b4a678ac3b097a6ec01906_3454503512785641159.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/76361817/cbc5e9b107a51aceb76dce8b834bdbb6_3105226355694069667.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/03/91006211/7fc15e81ff59c995fa4f879a7bfae9c8_69976860617641256.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/91006211/1be4e668c0dfeb17d18a842c4f85c93e_6231494208380658180.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/91006211/0d3b7e139551dbcf21f0f873635d0ae4_1407264053215478515.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/81417673/69ecd56d8fcb5c508eb97f5de33bcab1_528022664381526258.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/03/75216984/e8e8090954760cc6fe95d431b64b4cd5_7521615285836602789.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/81417673/a591dc36d3aafa15e39dd3fbbd863b64_6176622863951369414.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/50494840/85c33411e39474714f79b4b1841b895f_5535109047892991329.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/50494840/ad7e783fa7453ce1b2a1deb84b64aa3f_6443177772472048397.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/73514954/eb48190bf0035b5f4f8c866d973f8cf9_5062986070884601887.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/77124895/dc5e2301378ae5827401dc3c0dce6fce_2232950510869979022.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/03/72350798/9fc6f1ace0b879076449c69d6ee217db_2413463153380819103.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/77124895/e455ac814851956746eb0d94a0fb84bc_149985680841563428.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/77124895/a4cd255549b5021b7a9f4d596a6d406a_1622258159849132188.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/03/73514954/7458c61ddceabc89c5e470cc58cafaf0_3764731769924571495.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/91006211/4d32357f66b728c54653cec0d54dac45_2488262186206727523.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/50494840/8b49bdb94378dbf2ba7d79fc962e278b_2455004414960406501.png?x-oss-process=image/quality,q_75/resize,s_120",
            "https://uploadstatic.mihoyo.com/bh3-wiki/2021/09/04/75216984/379bd674d69ab0f4c1fb7a3f01650c2c_1993810691240158629.png?x-oss-process=image/quality,q_75/resize,s_120"
        )
        val regex = Regex("[^\u4e00-\u9fa5]*(\\S+)$")
        Jsoup.parse(html).getElementsByClass("collection-avatar__item").forEachIndexed { index, element ->
            run {
                val title = element.child(1).text()
                val name = regex.find(title)?.groupValues?.get(1)?.replace( Regex("[^a-zA-Z0-9\u4E00-\u9FA5]+"), "")
                val src = srcList[index]
                write(src, "src/main/resources/img/bh3/avatar/$name.png")
            }
        }
    }

    private fun write(url: String, path: String) {
        val img = HttpUtil.getInputStream(url)
        val file = File(path)
        file.outputStream().use { output -> img.copyTo(output) }
    }


}
