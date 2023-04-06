package org.hff.miraiomnitrix.db.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.io.Serializable

@TableName("`prompt`")
data class Prompt(
    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    var id: Int?,
    /** prompt名称 */
    @TableField("`name`")
    val name: String,
    /** prompt正文 */
    @TableField("`content`")
    var content: String,
): Serializable
