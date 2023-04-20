package org.hff.miraiomnitrix.db.entity

import com.baomidou.mybatisplus.annotation.*
import org.hff.miraiomnitrix.common.BooleanEnum
import java.io.Serializable

@TableName("`niuzi`")
data class Niuzi(
    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    var id: Int?,
    /** 群号 */
    val groupId: Long,
    /** qq号 */
    val qq: Long,
    /** 长度 */
    var length: Double,
    /** 是否完成每日 */
    @TableField("`is_day`")
    var isDay: BooleanEnum,
) : Serializable
