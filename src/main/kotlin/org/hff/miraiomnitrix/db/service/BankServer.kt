package org.hff.miraiomnitrix.db.service

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import org.hff.miraiomnitrix.db.entity.Bank
import org.hff.miraiomnitrix.db.mapper.BankMapper
import org.springframework.stereotype.Service

@Service
open class BankServer : ServiceImpl<BankMapper, Bank>() {

}
