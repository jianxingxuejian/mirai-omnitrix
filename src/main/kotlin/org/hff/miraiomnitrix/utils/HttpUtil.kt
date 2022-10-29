package org.hff.miraiomnitrix.utils

import cn.hutool.json.JSONUtil
import org.hff.miraiomnitrix.config.HttpProperties
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.ProxySelector
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpConnectTimeoutException
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

object HttpUtil {
    private val httpProperties = SpringUtil.getBean(HttpProperties::class.java)

    private val httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()
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

    fun getString(url: String): HttpResponse<String>? = try {
        httpClient.send(requestGet(url), HttpResponse.BodyHandlers.ofString())
    } catch (e: HttpConnectTimeoutException) {
        null
    }

    fun getString(url: String, token: String?): HttpResponse<String> {
        val request = HttpRequest.newBuilder(URI.create(url))
            .GET()
            .setHeader("authorization", token)
            .build()
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    }

    fun postString(url: String, data: Map<String, String?>, token: String?): HttpResponse<String> {
        val request = HttpRequest.newBuilder(URI.create(url))
            .POST(mapToJson(data))
            .setHeader("authorization", token)
            .build()
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    }

    fun postString(url: String, token: String?): HttpResponse<String> {
        val request = HttpRequest.newBuilder(URI.create(url))
            .POST(HttpRequest.BodyPublishers.ofString(url))
            .setHeader("authorization", token)
            .build()
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    }

    fun getString(url: String, token: String?, cookie: String?): HttpResponse<String> {
        val request = HttpRequest.newBuilder(URI.create(url))
            .GET()
            .setHeader("authorization", token)
            .setHeader("cookie", cookie)
            .build()
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    }

    fun getStringByProxy(url: String): HttpResponse<String>? =
        proxyClient?.send(requestGet(url), HttpResponse.BodyHandlers.ofString())

    fun getInputStream(url: String): HttpResponse<InputStream> =
        httpClient.send(requestGet(url), HttpResponse.BodyHandlers.ofInputStream())

    fun getInputStreamByProxy(url: String): HttpResponse<InputStream>? =
        proxyClient?.send(requestGet(url), HttpResponse.BodyHandlers.ofInputStream())

    fun postStringByProxy(url: String, data: Map<String, String?>, token: String?): HttpResponse<String>? {
        println(JSONUtil.parse(data).toString())
        val request = HttpRequest.newBuilder(URI.create(url))
            .POST(mapToJson(data))
            .setHeader("Content-Type", "application/json")
            .setHeader("authorization", token)
            .build()
        return proxyClient?.send(request, HttpResponse.BodyHandlers.ofString())
    }

    private fun requestGet(url: String) = HttpRequest.newBuilder(URI.create(url)).GET().build()

    private fun mapToJson(data: Map<String, String?>) =
        HttpRequest.BodyPublishers.ofString(JSONUtil.parse(data).toString())

}