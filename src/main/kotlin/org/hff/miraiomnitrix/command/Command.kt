package org.hff.miraiomnitrix.command

import org.springframework.stereotype.Component

@Component
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Command(
    /** 指令名称列表 */
    val name: Array<String>
)