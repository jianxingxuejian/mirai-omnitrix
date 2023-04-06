package org.hff.miraiomnitrix.utils

import org.hff.miraiomnitrix.common.MyException
import org.hff.miraiomnitrix.config.HttpProperties
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.ProxySelector
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers.ofString
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers.ofInputStream
import java.net.http.HttpResponse.BodyHandlers.ofString
import java.time.Duration

object HttpUtil {

    private val proxy = SpringUtil.getBean(HttpProperties::class).proxy

    private val httpClient = createHttpClient(10)
    private val proxyClient = createHttpClient(20, proxy)

    private fun createHttpClient(timeout: Long, proxy: HttpProperties.Proxy? = null): HttpClient {
        val builder = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(timeout))
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .version(HttpClient.Version.HTTP_1_1)
        if (proxy?.host != null && proxy.port != null) {
            builder.proxy(ProxySelector.of(InetSocketAddress(proxy.host, proxy.port)))
        }
        return builder.build()
    }


    fun getStringRes(url: String): HttpResponse<String> = httpClient.send(requestGet(url), ofString())

    fun getString(url: String): String = getStringRes(url).tryGetBody()

    fun getStringRes(url: String, headers: Map<String, String>): HttpResponse<String> =
        httpClient.send(requestGet(url, headers), ofString())

    fun getString(url: String, headers: Map<String, String>): String = getStringRes(url, headers).tryGetBody()

    fun getStringResByProxy(url: String): HttpResponse<String> = proxyClient.send(requestGet(url), ofString())

    fun getStringByProxy(url: String): String = getStringResByProxy(url).tryGetBody()

    fun getStringResByProxy(url: String, headers: Map<String, String>): HttpResponse<String> =
        proxyClient.send(requestGet(url, headers), ofString())

    fun getStringByProxy(url: String, headers: Map<String, String>): String =
        getStringResByProxy(url, headers).tryGetBody()

    fun getInputStreamRes(url: String): HttpResponse<InputStream> = httpClient.send(requestGet(url), ofInputStream())

    fun getInputStream(url: String): InputStream = getInputStreamRes(url).tryGetBody()

    fun getInputStreamResByProxy(url: String): HttpResponse<InputStream> =
        proxyClient.send(requestGet(url), ofInputStream())

    fun getInputStreamByProxy(url: String): InputStream = getInputStreamResByProxy(url).tryGetBody()

    fun postStringRes(url: String, data: Any): HttpResponse<String> =
        httpClient.send(requestPost(url, data), ofString())

    fun postString(url: String, data: Any): String = postStringRes(url, data).tryGetBody()

    fun postStringRes(url: String, data: Any, headers: Map<String, String>): HttpResponse<String> =
        httpClient.send(requestPost(url, data, headers), ofString())

    fun postStringResByProxy(url: String, data: Any, headers: Map<String, String>): HttpResponse<String> =
        proxyClient.send(requestPost(url, data, headers), ofString())

    fun postFormResByProxy(url: String, file: RequestFile, headers: Map<String, String>): HttpResponse<String> =
        proxyClient.send(requestPostForm(url, file, headers), ofString())

    fun postFormByProxy(url: String, file: RequestFile, headers: Map<String, String>): String =
        postFormResByProxy(url, file, headers).tryGetBody()

    private fun requestGet(url: String) = requestBuilder(url).GET().build()

    private fun requestGet(url: String, headers: Map<String, String>) =
        requestBuilder(url).GET().addHeaders(headers).build()

    private fun requestPost(url: String, data: Any) = requestBuilder(url)
        .POST(ofString(JsonUtil.parse(data)))
        .header("Content-Type", "application/json")
        .build()

    private fun requestPost(url: String, data: Any, headers: Map<String, String>) = requestBuilder(url)
        .POST(ofString(JsonUtil.parse(data)))
        .header("Content-Type", "application/json")
        .addHeaders(headers)
        .build()

    private fun requestPostForm(url: String, file: RequestFile, headers: Map<String, String>): HttpRequest {
        val body = buildMultiPartFormData(file.name, file.filename, file.mimeType, file.fileBytes)
        return requestBuilder(url)
            .POST(HttpRequest.BodyPublishers.ofByteArray(body))
            .header("Content-Type", "multipart/form-data; boundary=boundary123")
            .addHeaders(headers)
            .build()
    }

    private fun buildMultiPartFormData(
        name: String,
        filename: String,
        mimeType: String,
        fileBytes: ByteArray
    ): ByteArray {
        val buffer = ByteArrayOutputStream()
        buffer.apply {
            write("--boundary123\r\n".toByteArray())
            write("Content-Disposition: form-data; name=\"$name\"; filename=\"$filename\"\r\n".toByteArray())
            write("Content-Type: $mimeType\r\n\r\n".toByteArray())
            write(fileBytes)
            write("\r\n--boundary123--\r\n".toByteArray())
        }
        return buffer.toByteArray()
    }

    private fun <T : Any> HttpResponse<T>.tryGetBody(): T {
        if (statusCode() != 200) throw MyException("错误码: ${statusCode()}，错误信息: ${body()}")
        return body()
    }

    private fun requestBuilder(url: String): HttpRequest.Builder = HttpRequest.newBuilder(URI.create(url))

    private fun HttpRequest.Builder.addHeaders(headers: Map<String, String>): HttpRequest.Builder =
        apply { headers.forEach { (name, value) -> setHeader(name, value) } }

}

class RequestFile(
    val name: String,
    val filename: String,
    val mimeType: String,
    val fileBytes: ByteArray
)
