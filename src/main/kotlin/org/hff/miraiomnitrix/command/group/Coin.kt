package org.hff.miraiomnitrix.command.group

import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.GroupCommand
import org.hff.miraiomnitrix.db.service.BankService
import org.springframework.boot.CommandLineRunner
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

@EnableScheduling
@Command(name = ["coin"])
class Coin(private val bankService: BankService) : GroupCommand, CommandLineRunner {

    // 作为管理员，我希望持有50%的幻书币，当数量超过时，我会降低幻书币的买进价格，当数量不足时，我会提高幻书币的售出价格
    // 幻书币的基础价格是500金币/枚，从2023年4月7日作为初始日期，每天通胀1%
    private val originDate = LocalDate.of(2023, 4, 7)
    private var buyPrice = 1000
    private var sellPrice = 1000

    private val cache = hashSetOf<Long>()

    override suspend fun GroupMessageEvent.execute(args: List<String>): Message? {
        return when (args.firstOrNull()) {
            null -> bankService.getByQq(sender.id)
                .run { At(sender) + "当前持有：幻书币$coin，金币$money\n买进价格：$buyPrice，卖出价格：$sellPrice" }

            "买" -> {
                val number = args.getOrNull(1)?.toIntOrNull() ?: return "参数错误".toPlainText()
                At(sender) + sender.buy(number)
            }

            "卖" -> {
                val number = args.getOrNull(1)?.toIntOrNull() ?: return "参数错误".toPlainText()
                At(sender) + sender.sell(number)
            }

            "签到" -> At(sender) + sender.checkIn()
            else -> null
        }
    }

    private fun Member.buy(number: Int): String {
        val user = bankService.getByQq(id).apply {
            if (money < buyPrice * number) return "杂鱼，你的钱不够"
        }
        val bot = bankService.getByQq(0).apply {
            if (coin < number) return "服务器的幻书币只剩下${coin}个了"
        }
        val money = buyPrice * number
        user.coin += number
        user.money -= money
        bot.coin -= number
        bankService.updateById(user)
        bankService.updateById(bot)
        calcPrice()
        return "购买了${number}枚幻书币，花费${money}块钱。现在共计${user.coin}枚幻书币，${user.money}块钱。"
    }

    private fun Member.sell(number: Int): String {
        val user = bankService.getByQq(id).apply {
            if (coin < number) return "杂鱼，你的幻书币不够"
        }
        val bot = bankService.getByQq(0)
        user.coin -= number
        val money = buyPrice * number
        user.money += money
        bot.coin += number
        bankService.updateById(user)
        bankService.updateById(bot)
        calcPrice()
        return "卖出了${number}枚幻书币，花费${money}块钱。现在共计${user.coin}枚幻书币，${user.money}块钱。"
    }

    private fun Member.checkIn(): String {
        if (cache.contains(id)) return "今天已经签到了"
        val random = Random.nextInt(-1000, 10000)
        bankService.getByQq(id).apply { money += random }.let(bankService::updateById)
        cache.add(id)
        if (random < 0) return "真倒霉，钱包被偷走${random}块钱"
        if (random == 0) return "今日无事发生"
        if (random < 1000) return "马路上捡到${random}块钱"
        if (random < 5000) return "恭喜你，今日在女装餐厅打工挣了${random}块钱"
        return "真幸运，买彩票赚了${random}块钱"
    }

    override fun run(vararg args: String?) {
        bankService.getByQq(0)
        calcPrice()
    }

    private fun calcPrice() {
        val bankList = bankService.list()
        val bank = bankList.first { it.qq == 0L }
        val sum = bankList.sumOf { it.coin }
        val rate = if (sum == 0L) 0.0 else bank.coin / sum.toDouble()
        val day = ChronoUnit.DAYS.between(originDate, LocalDate.now()).toInt()
        val basic = 1000 * 1.01.pow(day)
        val calc = basic * (1.5 - rate)
        buyPrice = basic.let { if (rate > 0.5) calc else it }.roundToInt()
        sellPrice = basic.let { if (rate < 0.5) calc else it }.roundToInt()
    }

    @Scheduled(cron = "0 0 6 * * ?")
    fun listen() {
        cache.clear()
    }

}
