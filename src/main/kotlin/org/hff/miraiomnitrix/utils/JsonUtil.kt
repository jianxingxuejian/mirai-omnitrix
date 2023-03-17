package org.hff.miraiomnitrix.utils

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import org.hff.miraiomnitrix.exception.MyException

object JsonUtil {

    val gson = Gson()

    fun getStr(json: String, key: String): String = getKey(json, key).asString

    fun getObj(json: String, key: String): JsonObject = getKey(json, key).asJsonObject

    fun getArray(json: String): JsonArray = parse(json).asJsonArray

    fun getArray(json: String, key: String): JsonArray = getKey(json, key).asJsonArray

    inline fun <reified T> fromJson(json: String): T = gson.fromJson(json, object : TypeToken<T>() {}.type)

    inline fun <reified T : Any?> fromJson(json: String, key: String): T {
        val element = getKey(json, key)
        if (element.isJsonArray) {
            return gson.fromJson(element.asJsonArray, object : TypeToken<T>() {}.type)
        } else if (element.isJsonObject) {
            return gson.fromJson(element.asJsonObject, object : TypeToken<T>() {}.type)
        }
        throw MyException("json解析失败")
    }

    fun parse(data: Any) = gson.toJson(data).toString()

    fun getKey(json: String, key: String): JsonElement =
        JsonParser.parseString(json).asJsonObject.get(key) ?: throw MyException("json解析失败，检查key")

    private fun parse(json: String): JsonElement =
        try {
            JsonParser.parseString(json)
        } catch (e: Exception) {
            throw MyException("json解析失败")
        }

}

fun JsonObject.getAsStr(key: String): String = this.get(key).asString

fun JsonElement.get(key: String): JsonElement = this.asJsonObject.get(key)

fun JsonElement.getAsStr(key: String): String = this.asJsonObject.getAsStr(key)

fun JsonElement.getAsArray(key: String): JsonArray = this.asJsonObject.getAsJsonArray(key)
