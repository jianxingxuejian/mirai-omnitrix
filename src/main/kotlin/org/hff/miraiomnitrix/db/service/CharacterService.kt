package org.hff.miraiomnitrix.db.service

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import org.hff.miraiomnitrix.db.entity.Character
import org.hff.miraiomnitrix.db.mapper.CharacterMapper
import org.springframework.stereotype.Service

@Service
open class CharacterService : ServiceImpl<CharacterMapper, Character>() {

    fun getCharactersName(): String {
        return "可用角色如下：" + super.list()
            .map { it.name }.joinToString(",")
    }

    fun getCountByName(name: String): Long {
        return super.ktQuery()
            .eq(Character::name, name)
            .count()
    }

    fun get(name: String): Character? {
        return super.ktQuery()
            .eq(Character::name, name)
            .one()
    }

    fun add(name: String, externalId: String): Boolean {
        val character = Character()
        character.name = name
        character.externalId = externalId
        return super.save(character)
    }

    fun del(name: String): Boolean {
        return super.ktUpdate()
            .eq(Character::name, name)
            .remove()
    }

    fun edit(name: String, externalId: String): Boolean {
        return super.ktUpdate()
            .set(Character::externalId, externalId)
            .eq(Character::name, name)
            .update()
    }
}