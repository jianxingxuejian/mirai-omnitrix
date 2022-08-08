package org.hff.miraiomnitrix

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@EnableCaching
@SpringBootApplication
@ConfigurationPropertiesScan
class MiraiOmnitrixApplication

fun main(args: Array<String>) {
    runApplication<MiraiOmnitrixApplication>(*args)
}
