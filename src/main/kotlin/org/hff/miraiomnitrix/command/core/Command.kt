package org.hff.miraiomnitrix.command.core

import org.springframework.stereotype.Component

@Component
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Command(
    /** 指令名称列表 */
    val name: Array<String>,
    /** 是否需要指令头，默认为true */
    val isNeedHeader: Boolean = true,
)


