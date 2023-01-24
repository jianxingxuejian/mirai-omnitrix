package org.hff.miraiomnitrix.utils

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import kotlin.reflect.KClass

object JsonUtil {
    private val gson = Gson()

    fun getString(json: String) = JsonParser.parseString(json).asJsonObject

    fun getString(json: String, key: String): JsonElement = JsonParser.parseString(json).asJsonObject.get(key)

    fun <T : Any> fromJson(json: String, clazz: KClass<T>): T = gson.fromJson(json, clazz.java)

    fun <T : Any> fromJson(json: String, key: String, clazz: KClass<T>): T =
        gson.fromJson(getString(json, key), clazz.java)

    fun <T : Any> fromJson(json: JsonElement, clazz: KClass<T>): T = gson.fromJson(json, clazz.java)
}