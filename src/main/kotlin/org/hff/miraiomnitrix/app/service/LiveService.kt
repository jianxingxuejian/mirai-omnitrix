package org.hff.miraiomnitrix.app.service

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import org.hff.miraiomnitrix.app.entity.Live
import org.hff.miraiomnitrix.app.mapper.LiveMapper
import org.springframework.stereotype.Service

@Service
open class LiveService : ServiceImpl<LiveMapper, Live>() {
}