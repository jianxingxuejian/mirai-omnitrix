package org.hff.miraiomnitrix.db.service

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import org.hff.miraiomnitrix.db.entity.DomainName
import org.hff.miraiomnitrix.db.mapper.DomainNameMapper
import org.springframework.stereotype.Service

@Service
open class DomainNameService : ServiceImpl<DomainNameMapper, DomainName>() {

    fun add(domainName: String, state: Int) {
        val count = ktQuery().eq(DomainName::domainName, domainName).count()
        if (count != 0L) return
        save(DomainName(null, domainName, state))
    }

}
