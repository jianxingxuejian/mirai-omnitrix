package org.hff.miraiomnitrix.utils

import net.mamoe.mirai.contact.Member

private const val QQ_URL = "https://q1.qlogo.cn/g?b=qq&s=640&nk="

fun getQq(args: List<String>) = args.find { it.startsWith("@") }?.substring(1)?.toLong()

fun getQq(args: List<String>, sender: Member) = getQq(args) ?: sender.id

fun getQqImg(args: List<String>) = HttpUtil.getInputStream(QQ_URL + getQq(args))

fun getQqImg(args: List<String>, sender: Member) = HttpUtil.getInputStream(QQ_URL + getQq(args, sender))