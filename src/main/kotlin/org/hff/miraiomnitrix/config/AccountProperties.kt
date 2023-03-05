package org.hff.miraiomnitrix.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("account")
data class AccountProperties(
    /** https://beta.character.ai 登陆后F12查看请求头的authorization */
    val characterAiToken: String?,
    /** saucenao搜图api_key https://saucenao.com/user.php?page=search-api */
    val saucenaoKey: String?,
    /** bangumi番组计划网页cookie https://bgm.tv */
    val bgmCookie: String?,
    /** https://platform.openai.com/account/api-keys */
    val openaiApiKey: String?,
    /** 微软azure语音服务密钥 */
    val azureSpeechKey: String?,
    /** 微软azure语音服务区域 */
    val azureSpeechRegion: String?,
    /** 谷歌搜索api-key https://developers.google.com/custom-search/v1/overview */
    val googleSearchKey: String?,
    /** 必应搜索 */
    val bingSearchKey: String?,
)
