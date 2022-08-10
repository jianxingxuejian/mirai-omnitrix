package org.hff.miraiomnitrix.command

import org.springframework.stereotype.Component

@Component
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Command(
    val name: Array<String>,
    val isNeedHeader: Boolean = true,
)
