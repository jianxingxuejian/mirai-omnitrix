package org.hff.miraiomnitrix.db.service

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import org.hff.miraiomnitrix.db.entity.Prompt
import org.hff.miraiomnitrix.db.mapper.PromptMapper
import org.springframework.stereotype.Service

@Service
open class PromptService : ServiceImpl<PromptMapper, Prompt>()
