package org.hff.miraiomnitrix.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("account")
data class AccountProperties(
    /** character AI token  */
    val characterAiToken: String?,
    /** saucenao api key */
    val saucenaoKey: String?,
    /** bangumi web cookie */
    val bgmCookie: String?
)
