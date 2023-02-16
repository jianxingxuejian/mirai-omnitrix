package org.hff.miraiomnitrix.db.service

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import org.hff.miraiomnitrix.db.entity.Bgm
import org.hff.miraiomnitrix.db.mapper.BgmMapper
import org.springframework.stereotype.Service

@Service
open class BgmService : ServiceImpl<BgmMapper, Bgm>()
