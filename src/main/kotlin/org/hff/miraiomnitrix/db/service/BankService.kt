package org.hff.miraiomnitrix.db.service

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import org.hff.miraiomnitrix.common.MyException
import org.hff.miraiomnitrix.db.entity.Bank
import org.hff.miraiomnitrix.db.mapper.BankMapper
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
open class BankService : ServiceImpl<BankMapper, Bank>() {

    fun getByQq(qq: Long): Bank {
        val bank = ktQuery().eq(Bank::qq, qq).oneOpt().getOrNull()
        if (bank == null) {
            val newBank = Bank(null, qq, 0, 0)
            val save = save(newBank)
            if (!save) throw MyException("新建账号失败")
            return newBank
        }
        return bank
    }
}
