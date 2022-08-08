package org.hff.miraiomnitrix.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("proxy")
data class ProxyProperties(
    /**
     * 代理ip.
     */
    val host: String,
    /**
     * 代理端口.
     */
    val port: Int,
)