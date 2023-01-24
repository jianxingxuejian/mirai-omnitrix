package org.hff.miraiomnitrix.app.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.io.Serializable

@TableName("`live`")
data class Live(
    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    var id: Int?,

    /** qq号 */
    var qq: Long,

    /** 群号 */
    var groupId: Long,

    /** uid */
    var uid: Long,

    /** 直播间id */
    var roomId: Int
) : Serializable
