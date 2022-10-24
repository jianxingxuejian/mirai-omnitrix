package org.hff.miraiomnitrix.command.any

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChainBuilder
import org.hff.miraiomnitrix.app.entity.Bgm
import org.hff.miraiomnitrix.app.service.BgmService
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.result.ResultMessage
import org.hff.miraiomnitrix.utils.HttpUtil

@Command(name = ["番剧推荐", "bgm"])
class Bgm(private val bgmService: BgmService) : AnyCommand {

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun execute(
        sender: User,
        message: MessageChain,
        subject: Contact,
        args: List<String>
    ): ResultMessage? {
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

        val list = wrapper.last("ORDER BY RAND() LIMIT $num").list()

            list.forEach {
                GlobalScope.launch {
                    val msg = MessageChainBuilder()
                    if (it.imgUrl != null && !it.imgUrl.equals("https:/img/no_icon_subject.png")) {
                        val response = HttpUtil.getInputStreamByProxy(it.imgUrl!!)
                        if (response?.statusCode() == 200) {
                            val image = subject.uploadImage(response.body())
                            msg.append(image)
                        }
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
}