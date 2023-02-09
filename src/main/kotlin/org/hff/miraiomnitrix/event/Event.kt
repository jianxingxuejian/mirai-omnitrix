package org.hff.miraiomnitrix.event

import org.springframework.stereotype.Component

@Component
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Event(
    /** 优先级 */
    val priority: Int
)
