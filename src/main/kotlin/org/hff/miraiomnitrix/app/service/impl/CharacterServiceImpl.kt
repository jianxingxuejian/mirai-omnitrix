package org.hff.miraiomnitrix.app.service.impl

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import org.hff.miraiomnitrix.app.entity.Character
import org.hff.miraiomnitrix.app.mapper.CharacterMapper
import org.hff.miraiomnitrix.app.service.CharacterService
import org.springframework.stereotype.Service

@Service
open class CharacterServiceImpl : ServiceImpl<CharacterMapper, Character>(), CharacterService {

    override fun getCharactersName(): String {
        return "可用角色如下：" + super<CharacterService>.list()
            .map { it.name }.joinToString(",")
    }

    override fun getCountByName(name: String): Long {
        return super<CharacterService>.ktQuery()
            .eq(Character::name, name)
            .count()
    }

    override fun get(name: String): Character? {
        return super<CharacterService>.ktQuery()
            .eq(Character::name, name)
            .one()
    }

    override fun add(name: String, externalId: String): Boolean {
        val character = Character()
        character.name = name
        character.externalId = externalId
        return super<CharacterService>.save(character)
    }

    override fun del(name: String): Boolean {
        return super<CharacterService>.ktUpdate()
            .eq(Character::name, name)
            .remove()
    }

    override fun edit(name: String, externalId: String): Boolean {
        return super<CharacterService>.ktUpdate()
            .set(Character::externalId, externalId)
            .eq(Character::name, name)
            .update()
    }
}