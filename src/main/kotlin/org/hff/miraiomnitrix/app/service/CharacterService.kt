package org.hff.miraiomnitrix.app.service

import com.baomidou.mybatisplus.extension.service.IService
import org.hff.miraiomnitrix.app.entity.Character

interface CharacterService : IService<Character> {

    fun getCharactersName(): String

    fun getCountByName(name: String): Long

    fun get(name: String): Character?

    fun add(name: String, externalId: String): Boolean

    fun del(name: String): Boolean

    fun edit(name: String, externalId: String): Boolean
}