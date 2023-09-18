package org.hff.miraiomnitrix.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("http")
data class HttpProperties(
    /** 代理配置 */
    val proxy: Proxy
) {
    data class Proxy(
        /** 代理ip */
        val host: String?,
        /** 代理端口 */
        val port: Int?
    )
}
