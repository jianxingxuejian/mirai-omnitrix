package org.hff.miraiomnitrix.db.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.io.Serializable

@TableName("`bank`")
data class Bank(
    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    val id: Int?,
    /** qq号 */
    @TableField("`qq`")
    val qq: Long,
    /** 持有的钱 */
    @TableField("`money`")
    val money: Long,
    /** 持有的幻书币 */
    @TableField("`coin`")
    val coin: String,
): Serializable
