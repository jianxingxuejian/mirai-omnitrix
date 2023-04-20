package org.hff.miraiomnitrix.utils

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.engine.java.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.hff.miraiomnitrix.common.MyException
import org.hff.miraiomnitrix.config.HttpProperties
import java.io.InputStream
import java.net.http.HttpClient

object HttpUtil {

    private val proxyProperties = SpringUtil.getBean(HttpProperties::class).proxy

    private fun buildClient(proxyAddress: String? = null) = HttpClient(Java) {
        engine {
            threadsCount = 8
            pipelining = true
            proxy = proxyAddress?.let { ProxyBuilder.http(proxyAddress) }
            protocolVersion = HttpClient.Version.HTTP_1_1
        }
    }

    private val client = buildClient()
    private val proxyClient = proxyProperties.run {
        if (host != null && port != null) buildClient("https://$host:$port")
        else client
    }

    private fun getClient(isProxy: Boolean) = if (isProxy) proxyClient else client

    suspend inline fun <reified T> getJson(
        url: String,
        headers: Map<String, String>? = null,
        isProxy: Boolean = false
    ): T = getString(url, headers, isProxy).let(JsonUtil::fromJson)

    suspend fun getString(url: String, headers: Map<String, String>? = null, isProxy: Boolean = false) =
        get(url, headers, isProxy).tryGetBody<String>()

    suspend fun getInputStream(
        url: String,
        headers: Map<String, String>? = null,
        isProxy: Boolean = false
    ): InputStream = get(url, headers, isProxy).tryGetBody<ByteArray>().inputStream()

    suspend fun get(url: String, headers: Map<String, String>? = null, isProxy: Boolean = false) =
        getClient(isProxy).get(url, headers)

    private suspend fun io.ktor.client.HttpClient.get(url: String, headers: Map<String, String>? = null) =
        get(url) { addHeaders(headers) }

    suspend inline fun <reified T> postJson(
        url: String,
        data: Any? = null,
        headers: Map<String, String>? = null,
        isProxy: Boolean = false
    ): T = postString(url, data, headers, isProxy).let(JsonUtil::fromJson)

    suspend fun postString(
        url: String,
        data: Any? = null,
        headers: Map<String, String>? = null,
        isProxy: Boolean = false
    ) = post(url, data, headers, isProxy).tryGetBody<String>()

    suspend fun postInputStream(
        url: String,
        data: Any? = null,
        headers: Map<String, String>? = null,
        isProxy: Boolean = false
    ): InputStream = post(url, data, headers, isProxy).tryGetBody<ByteArray>().inputStream()

    suspend fun post(url: String, data: Any? = null, headers: Map<String, String>? = null, isProxy: Boolean = false) =
        getClient(isProxy).post(url, data, headers)

    private suspend inline fun io.ktor.client.HttpClient.post(
        url: String,
        data: Any? = null,
        headers: Map<String, String>? = null
    ) = post(url) {
        contentType(ContentType.Application.Json)
        data?.let { setBody(JsonUtil.parse(it)) }
        addHeaders(headers)
    }

    suspend inline fun <reified T> formJson(
        url: String,
        file: RequestFile,
        headers: Map<String, String>? = null,
        isProxy: Boolean = false
    ): T = formString(url, file, headers, isProxy).let(JsonUtil::fromJson)

    suspend inline fun <reified T> formJson(
        url: String,
        data: Map<String, String>,
        headers: Map<String, String>? = null,
        isProxy: Boolean = false
    ): T = formString(url, data, headers, isProxy).let(JsonUtil::fromJson)

    suspend fun formString(
        url: String,
        file: RequestFile,
        headers: Map<String, String>? = null,
        isProxy: Boolean = false
    ) = form(url, file, headers, isProxy).tryGetBody<String>()

    suspend fun formString(
        url: String,
        data: Map<String, String>,
        headers: Map<String, String>? = null,
        isProxy: Boolean = false
    ) = form(url, data, headers, isProxy).tryGetBody<String>()

    suspend fun form(url: String, file: RequestFile, headers: Map<String, String>? = null, isProxy: Boolean = false) =
        getClient(isProxy).submitFormWithBinaryData(url, buildFileFormData(file)) { addHeaders(headers) }

    suspend fun form(
        url: String,
        data: Map<String, String>,
        headers: Map<String, String>? = null,
        isProxy: Boolean = false
    ) = getClient(isProxy).submitForm(url, buildFormData(data)) { addHeaders(headers) }

    private fun buildFileFormData(file: RequestFile) = formData {
        append("file", file.fileBytes, Headers.build {
            append(HttpHeaders.ContentType, file.mimeType)
            append(HttpHeaders.ContentDisposition, "filename=\"${file.filename}\"")
        })
    }

    private fun buildFormData(data: Map<String, String>) = Parameters.build {
        data.forEach { (key, value) -> append(key, value) }
    }

    private fun HttpRequestBuilder.addHeaders(headers: Map<String, String>?) =
        headers { headers?.forEach { (key, value) -> append(key, value) } }

    private suspend inline fun <reified T> HttpResponse.tryGetBody(): T {
        if (status != HttpStatusCode.OK) throw MyException("请求失败: $status")
        return body()
    }

}

class RequestFile(
    val name: String = "image",
    val filename: String = "image.png",
    val mimeType: String = "image/png",
    val fileBytes: ByteArray
)
