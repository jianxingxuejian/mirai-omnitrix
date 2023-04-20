package org.hff.miraiomnitrix.db.service

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import org.hff.miraiomnitrix.db.entity.Niuzi
import org.hff.miraiomnitrix.db.mapper.NiuziMapper
import org.springframework.stereotype.Service

@Service
open class NiuziService : ServiceImpl<NiuziMapper, Niuzi>()
