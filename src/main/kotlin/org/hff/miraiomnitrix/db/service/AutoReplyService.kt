package org.hff.miraiomnitrix.db.service

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import org.hff.miraiomnitrix.db.entity.AutoReply
import org.hff.miraiomnitrix.db.mapper.AutoReplyMapper
import org.springframework.stereotype.Service

@Service
open class AutoReplyService : ServiceImpl<AutoReplyMapper, AutoReply>()
