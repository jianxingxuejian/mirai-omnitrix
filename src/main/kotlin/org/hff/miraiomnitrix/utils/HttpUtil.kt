package org.hff.miraiomnitrix.utils

import cn.hutool.json.JSONUtil
import org.hff.miraiomnitrix.config.HttpProperties
import org.hff.miraiomnitrix.utils.SpringUtil.getBean
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.ProxySelector
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

object HttpUtil {
    private val httpProperties = getBean(HttpProperties::class)

    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .version(HttpClient.Version.HTTP_1_1)
        .build()
    private var proxyClient: HttpClient? = null

    init {
        if (httpProperties != null) {
            val (host, port) = httpProperties.proxy
            if (host != null && port != null) {
                proxyClient = HttpClient.newBuilder()
                    .proxy(ProxySelector.of(InetSocketAddress(host, port)))
                    .build()
            }
        }
    }

    fun getString(url: String, headers: Map<String, String>? = null): HttpResponse<String> =
        httpClient.send(requestGet(url, headers), HttpResponse.BodyHandlers.ofString())

    fun getStringByProxy(url: String): HttpResponse<String>? =
        proxyClient?.send(requestGet(url), HttpResponse.BodyHandlers.ofString())

    fun getInputStream(url: String): HttpResponse<InputStream> =
        httpClient.send(requestGet(url), HttpResponse.BodyHandlers.ofInputStream())

    fun getInputStreamByProxy(url: String): HttpResponse<InputStream>? =
        proxyClient?.send(requestGet(url), HttpResponse.BodyHandlers.ofInputStream())

    fun postString(
        url: String,
        data: Map<String, String?>,
        headers: Map<String, String>? = null
    ): HttpResponse<String> =
        httpClient.send(requestPost(url, data, headers), HttpResponse.BodyHandlers.ofString())

    fun postStringByProxy(
        url: String,
        data: Map<String, String?>,
        headers: Map<String, String>? = null
    ): HttpResponse<String>? {
        return proxyClient?.send(requestPost(url, data, headers), HttpResponse.BodyHandlers.ofString())
    }

    private fun requestGetBuilder(url: String) = HttpRequest.newBuilder(URI.create(url)).GET()

    private fun requestGet(url: String, headers: Map<String, String>? = null) =
        if (headers == null) {
            requestGetBuilder(url).build()
        } else {
            val builder = requestGetBuilder(url)
            headers.forEach { (name, value) -> builder.setHeader(name, value) }
            builder.build()
        }

    private fun requestPostBuilder(url: String, data: Map<String, String?>) =
        HttpRequest.newBuilder(URI.create(url))
            .POST(HttpRequest.BodyPublishers.ofString(JSONUtil.parse(data).toString()))

    private fun requestPost(url: String, data: Map<String, String?>, headers: Map<String, String>?) =
        if (headers == null) {
            requestPostBuilder(url, data).build()
        } else {
            val builder = requestPostBuilder(url, data)
            headers.forEach { (name, value) -> builder.setHeader(name, value) }
            builder.build()
        }

}