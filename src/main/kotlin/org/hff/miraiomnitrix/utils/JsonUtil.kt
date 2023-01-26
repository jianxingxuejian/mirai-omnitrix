package org.hff.miraiomnitrix.utils

import com.google.gson.*
import org.hff.miraiomnitrix.exception.MyException
import kotlin.reflect.KClass

object JsonUtil {
    private val gson = Gson()

    fun getStr(json: String) = parse(json).asString

    fun getStr(json: String, key: String): String = getKey(json, key).asString

    fun getStr(obj: JsonObject, key: String): String = obj.getAsStr(key)

    fun getStr(element: JsonElement, key: String): String = element.asJsonObject.getAsStr(key)

    fun getObj(json: String) = parse(json).asJsonObject

    fun getObj(json: String, key: String): JsonObject = getKey(json, key).asJsonObject

    fun getArray(json: String): JsonArray = parse(json).asJsonArray

    fun getArray(json: String, key: String): JsonArray = getKey(json, key).asJsonArray

    fun <T : Any> fromJson(json: String, clazz: KClass<T>): T = gson.fromJson(json, clazz.java)

    fun <T : Any> fromJson(json: String, key: String, clazz: KClass<T>): T =
        gson.fromJson(getObj(json, key), clazz.java)

    fun <T : Any> fromJson(json: JsonElement, clazz: KClass<T>): T = gson.fromJson(json, clazz.java)

    fun JsonObject.getAsStr(key: String): String = this.get(key).asString

    fun JsonElement.get(key: String): JsonElement = this.asJsonObject.get(key)

    fun JsonElement.getAsStr(key: String): String = this.asJsonObject.getAsStr(key)

    private fun getKey(json: String, key: String): JsonElement =
        JsonParser.parseString(json).asJsonObject.get(key) ?: throw MyException("json解析失败，检查key")

    private fun parse(json: String): JsonElement =
        try {
            JsonParser.parseString(json)
        } catch (e: Exception) {
            throw MyException("json解析失败")
        }


}