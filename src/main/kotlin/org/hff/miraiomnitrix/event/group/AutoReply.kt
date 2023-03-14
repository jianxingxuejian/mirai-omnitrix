package org.hff.miraiomnitrix.event.group

import com.google.common.util.concurrent.RateLimiter
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.event.Event
import org.hff.miraiomnitrix.event.EventResult
import org.hff.miraiomnitrix.event.GroupEvent
import org.hff.miraiomnitrix.event.next
import org.hff.miraiomnitrix.utils.getInfo
import org.hff.miraiomnitrix.utils.toImage
import org.hff.miraiomnitrix.utils.toText
import java.lang.management.ManagementFactory
import java.time.LocalTime
import java.util.concurrent.TimeUnit

@Event(priority = 2)
class AutoReply(private val permissionProperties: PermissionProperties) : GroupEvent {

    val limiterMap = hashMapOf<Long, RateLimiter>()

    private val textMap = mapOf(
        "day0" to "day0",
        "贴贴" to "贴贴",
        "嘻嘻" to "嘻嘻",
        "笨蛋" to "八嘎",
        "8" to "10",
        "吃饭了" to "好饿",
        "?" to "¿",
        "？" to "¿",
        "??" to "???",
        "？？" to "？？？",
        "好活" to "活好",
        "小黑子" to "你干嘛～～哎呦",
        "真的" to "假的",
        "假的" to "真的",
        "真的假的" to "你猜",
    )
    private val replyMap = mapOf(
        "炼" to "不许炼",
        "好累啊" to "累就对了",
        "涩涩" to "不可以涩涩",
        "不可以涩涩" to "不涩涩就挨打",
        "不准涩涩" to "不涩涩就挨打",
        "不许涩涩" to "不涩涩就挨打",
        "抱抱" to "抱抱",
    )
    private val imgMap = mapOf(
        "罕见" to "{F4F6D6BF-24C1-91D9-6E5E-8E2B5D487AF1}.png",
        "不够涩" to "{33B0B14D-32C3-CAAA-B43D-9B1604BE21F5}.png",
        "略略略" to "{492D406C-C102-6F66-E5BD-C59204E5032F}.gif",
        "impact重点照顾你" to "{40EB959D-80FF-1C7F-85A2-5D5E82B52C52}.gif",
        "菊花橄榄" to "{53DEA461-E77B-0C87-465E-A1A91883771D}.jpg",
        "一般" to "{440AE237-965E-1851-B50F-5DA8DC23B1DD}.jpg",
        "你为什么专拣这个事报道" to "{643A1B44-3713-0B6A-3F1C-CBB4924B6640}.jpg",
        "永雏塔菲" to "{0B5BD81D-8F06-C1F3-AD6F-B0A6D412E1CD}.gif",
        "原来如此" to "{A0392213-9F0E-DB50-CB2A-E8E3FAE84202}.jpg",
        "开摆" to "{ED8CB262-1879-85E9-CCAB-D4AB60A451B7}.gif",
        "太好玩了" to "{185BA6B9-54C8-5F3B-0505-8E96621FE0AD}.jpg",
        "头呢" to "{B147AABB-0734-1471-473B-87A7116B32A1}.jpg",
        "每日委托" to "{6AB7F2D0-4EB5-AD28-AED4-DB6E90CD26AB}.jpg",
        "邀约事件" to "{3E43EDBD-C3B9-0A58-C15D-0F5E1FD562A4}.gif",
        "可恶" to "{93FF64EA-F898-CEF2-AE55-F9EEE5E9D862}.jpg",
        "下班" to "{9649748E-B2C4-1EEC-A016-2C4F55FEFC4B}.jpg",
        "垃圾" to "{B3E43748-78A6-1E0C-C2CC-5423B5598E26}.jpg",
        "啊这" to "{C0AB46BC-7280-0A4B-2CD0-248A4A92B772}.jpg",
        "冲" to "{BF41CDAF-7C11-D6C4-3C67-7A17EE01E699}.jpg",
        "本群男女关系" to "{1B89DB09-4FAB-628D-5E8F-34CABC596C92}.jpg",
        "摆烂" to "{098AEBAD-06C4-B94A-A466-59697AAE3A0A}.jpg",
        "你是人吗" to "{9AEB4B28-9905-635B-E9DE-F92596C3F183}.jpg",
        "借点" to "{860EFF55-D490-6B28-B6C3-41ADC785666F}.jpg",
        "火星" to "{9AD711EE-7126-11F0-EA99-E9CF01173324}.jpg",
        "不行" to "{A7B2075C-CA08-8BD9-C7E5-0FB9E8F6A454}.jpg",
        "约吗" to "{588DEAB6-4A76-F6CF-9D39-735142CDC6CF}.jpg",
    ).mapValues { it.value.toImage() }
    private val regexMap = mapOf<String, () -> Message>(
        "爱丽丝在吗" to {
            getStatus().toText()
        },
        "别在这里发(电|癫)" to {
            listOf(
                "{5B8FA4E2-3AD7-1332-522E-804C956044A6}.jpg",
                "{57496E7C-A75D-BCEE-5049-4064C9B0093B}.jpg",
                "{305921B1-E709-F4F2-06C7-9E9EB88AAA55}.jpg",
            ).random().toImage()
        },
        "不够涩" to {
            listOf(
                "{BFBA5779-94F3-6106-91E5-4139BE7C39D8}.jpg",
                "{66C9BCCE-FC69-43F5-B0B3-BE9D4FF410B4}.jpg",
            ).random().toImage()
        },
        "可爱滴捏" to {
            listOf(
                "{1295CC27-B7AE-643D-F885-25768E2C9A41}.jpg",
                "{4424263C-D916-0AB4-3EA1-1C9DB1BA4772}.jpg",
                "{E7CD4B3F-CEEA-70ED-C955-D7EBEBD40190}.jpg",
                "{AFAE2A86-BED7-3A50-53CD-3AB77F686A37}.jpg",
            ).random().toImage()
        },
        ".*笑死.*" to {
            listOf(
                "{65929E6A-55E8-AF62-D73B-DA8F7F49EA81}.jpg",
                "{15F84B1C-F9A2-94F4-0F60-D68AD227FE00}.jpg",
                "{F19F61AC-BDB2-DCE7-33D0-36A436B5FBB2}.jpg",
                "{9E7D12FA-F8BB-12DC-C388-A74F4B554C18}.jpg",
            ).random().toImage()
        },
        ".*(v|V)我50.*" to { "{71ED101C-EB7F-DE4F-1089-002775821E2D}.jpg".toImage() },
        ".*要死了.*" to { "{763F39AA-0F22-E4F8-3993-512DC7A8A841}.jpg".toImage() },
        ".*(睡觉|晚安).*" to {
            if (LocalTime.now() in LocalTime.MIDNIGHT..LocalTime.of(6, 0))
                "{1DB34403-D6DA-5C2C-C3D2-EF86C4D5E7CF}.gif".toImage()
            else "你这个年龄段你睡得着觉?".toText()
        },
        "有没有.*" to { "{6E32B4F8-3EB6-AA93-47AF-3F8F6B245ECE}.jpg".toImage() },
        ".*能不能给.*" to { "{27339315-3AEC-C81D-ED68-E76E17518A59}.jpg".toImage() },
        "任何邪恶.*" to {
            listOf(
                "{E6ACDA96-0363-A1F6-E4B3-98D9C2443349}.jpg",
                "{EF917624-1C61-E78E-E58E-FA06BC6CA6AB}.jpg",
            ).random().toImage()
        },
        "如何评价.*" to { "{48019D08-C007-A1BD-9CED-3BFDF0FA03CA}.jpg".toImage() },
        "(op|OP)笑话" to {
            listOf(
                "{9D185535-BD52-8FEB-87A3-72C6A71ED77A}.jpg",
                "{A26B7EFF-6311-D89E-0DB4-CF56C9C43DB8}.jpg",
                "{D77E5009-AB97-27B8-F399-99DC92E5BA89}.jpg",
                "{6D1FCD80-F5AE-8275-2E11-7482BC81D4DC}.jpg",
                "{FDAD307E-4915-81AC-1591-3DCB2A15AC38}.jpg",
                "{93A38A21-2AB0-A3A8-39CD-6338DEA15609}.jpg",
                "{F70554AA-757A-647D-683D-677E7BC4B20D}.jpg",
                "{7B912FCE-2CA3-4D94-9F54-82E0BEA9BCB9}.jpg",
            ).random().toImage()
        },
        "丁真笑话" to {
            listOf(
                "{3A5C8B36-F7FF-77C7-BA8E-903CDCFEE7FE}.jpg",
                "{8F69040B-0B14-CAF9-F9BC-4B1442BDB4CB}.jpg",
            ).random().toImage()
        },
        "(黑人|尼哥)笑话" to {
            listOf(
                "{18FB7B7B-C54A-61CE-6BFE-229B5472D0A3}.jpg",
                "{683A6644-3A4F-A810-CAAE-2AF18859174F}.jpg",
                "{B5E8F634-676A-CD85-E12B-409CB9223688}.jpg",
            ).random().toImage()
        },
//        "(vtb|管人痴)笑话" to {
//            listOf(
//            ).random().toImage()
//        },
        ".*(泪目|哭了).*" to { "擦擦".toText() },
        "(?=.*晕).{0,5}" to { "{B51F2400-6BCC-EF9F-FC81-249DE7F7AA40}.png".toImage() },
        "(?=.*银趴)(?=.*加).*" to { "{A871CDDD-82BB-AF4C-A294-F7148C1952D0}.jpg".toImage() },
        "(?=.*纳西妲)(?=.*可爱).*" to { "{C8B92F29-A620-4567-8C34-57779FF04F5B}.gif".toImage() },
        "(?=.*迪希雅)(?=.*伤害)(?=.*低).*" to { "{9ECC0D00-71CB-D3A0-161A-1D2C9E0CBB23}.jpg".toImage() },
        ".*(dnf|DNF).*" to { "{63013DA6-416A-FEB9-2FD9-D9BB13798FEC}.jpg".toImage() },
        ".*贫乳.*" to { "{F07AB14E-2378-5B70-D0DE-FA749886AA5C}.jpg".toImage() },
        ".*没氪金.*" to { "{D08D8B99-03AD-A085-829E-95FB60B6186B}.jpg".toImage() },
        ".*还有救吗.*" to { "{93077556-7FD5-9A52-0AAF-79D88DB1AC21}.jpg".toImage() },
    ).mapKeys { it.key.toRegex() }

    override suspend fun handle(args: List<String>, event: GroupMessageEvent, isAt: Boolean): EventResult {
        if (permissionProperties.replyExcludeGroup.contains(event.group.id)) return next()
        val limiter =
            if (limiterMap.contains(event.group.id)) {
                limiterMap[event.group.id]
            } else {
                val limiter = RateLimiter.create(1.0)
                limiterMap[event.group.id] = limiter
                limiter
            }

        if (args.isEmpty()) return next()

        val (group, _, message) = event.getInfo()
        val arg = args[0]

        with(limiter!!) {
            when (arg) {
                in textMap.keys ->
                    textMap[arg]?.takeIf { tryAcquire() }?.let { group.sendMessage(it) }

                in replyMap.keys ->
                    replyMap[arg]?.takeIf { tryAcquire() }?.let { group.sendMessage(message.quote() + it) }

                in imgMap.keys ->
                    imgMap[arg]?.takeIf { tryAcquire() }?.let { group.sendMessage(it) }

                else -> regexMap.entries.find { it.key.matches(arg) }?.value?.takeIf { tryAcquire() }
                    ?.let { group.sendMessage(it.invoke()) }
            }
        }

        return next()
    }

    fun getStatus(): String {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val memory = "内存使用: ${totalMemory / 1024 / 1024}MB\n"
        val uptimeInMillis = ManagementFactory.getRuntimeMXBean().uptime
        val uptime = getUptime(uptimeInMillis)
        return memory + uptime
    }

    fun getUptime(time: Long): String {
        val days = TimeUnit.MILLISECONDS.toDays(time)
        val hours = TimeUnit.MILLISECONDS.toHours(time) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(time) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(time) % 60
        return "已运行: ${if (days > 0) "$days 天" else ""}${if (hours > 0) "$hours 小时 " else ""}${if (minutes > 0) "$minutes 分钟 " else ""}$seconds 秒\n"
    }
}
