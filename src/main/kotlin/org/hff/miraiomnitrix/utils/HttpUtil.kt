package org.hff.miraiomnitrix.utils

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

    fun getString(url: String): HttpResponse<String>? {
        val request = HttpRequest.newBuilder(URI.create(url)).GET().build()
        return try {
            val response = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            response.join()
        } catch (e: HttpConnectTimeoutException) {
            null
        }
    }

    fun getString(url: String, cookie: String): HttpResponse<String>? {
        val request = HttpRequest.newBuilder(URI.create(url)).GET()
            .setHeader("cookie", cookie).build()
        val response = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        return response.join()
    }

    fun getStringByProxy(url: String): HttpResponse<String>? {
        val request = HttpRequest.newBuilder(URI.create(url)).GET().build()
        val response = proxyClient?.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        return response?.join()
    }

    fun getInputStream(url: String): HttpResponse<InputStream>? {
        val request = HttpRequest.newBuilder(URI.create(url)).GET().build()
        val response = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
        return response.join()
    }

    fun getInputStreamByProxy(url: String): HttpResponse<InputStream>? {
        val request = HttpRequest.newBuilder(URI.create(url)).GET().build()
        val response = proxyClient?.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
        return response?.join()
    }


}