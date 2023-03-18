package org.hff.miraiomnitrix.db.entity

import com.baomidou.mybatisplus.annotation.*
import java.io.Serializable

@TableName("`auto_reply`")
data class AutoReply(
    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    val id: Int?,
    /** 类型 */
    @TableField("`type`")
    val type: ReplyEnum,
    /** 触发关键字 */
    @TableField("`keyword`")
    val keyword: String,
    /** 内容 */
    @TableField("`content`")
    val content: String,
) : Serializable

enum class ReplyEnum(private val value: Int) : IEnum<Int> {
    Text(1),
    Reply(2),
    Image(3),
    Regex(4);

    override fun getValue() = value
}
