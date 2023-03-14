package org.hff.miraiomnitrix.db.service

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import org.hff.miraiomnitrix.db.entity.Live
import org.hff.miraiomnitrix.db.mapper.LiveMapper
import org.springframework.stereotype.Service

@Service
open class LiveService : ServiceImpl<LiveMapper, Live>() {

    fun removeByQQ(qq: Long) = super.ktUpdate()
        .eq(Live::qq, qq)
        .remove()

    fun getListByGroup(groupId:Long): MutableList<Live> = super.ktQuery()
        .eq(Live::groupId, groupId)
        .list()
}
