package org.hff.miraiomnitrix.app.service

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import org.hff.miraiomnitrix.app.entity.Bgm
import org.hff.miraiomnitrix.app.mapper.BgmMapper
import org.springframework.stereotype.Service

@Service
open class BgmService : ServiceImpl<BgmMapper, Bgm>()
