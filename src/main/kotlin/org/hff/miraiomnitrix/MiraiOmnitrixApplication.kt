package org.hff.miraiomnitrix

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class MiraiOmnitrixApplication

fun main(args: Array<String>) {
    runApplication<MiraiOmnitrixApplication>(*args)
}
