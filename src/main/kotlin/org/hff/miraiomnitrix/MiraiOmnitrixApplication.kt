package org.hff.miraiomnitrix

import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

//@EnableAspectJAutoProxy
@SpringBootApplication
@ConfigurationPropertiesScan
@MapperScan("org.hff.miraiomnitrix.db.mapper")
class MiraiOmnitrixApplication

fun main(args: Array<String>) {
    runApplication<MiraiOmnitrixApplication>(*args)
}
