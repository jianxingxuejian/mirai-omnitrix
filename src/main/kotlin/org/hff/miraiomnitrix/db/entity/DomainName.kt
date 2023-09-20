package org.hff.miraiomnitrix.db.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.io.Serializable

@TableName("`domain_name`")
data class DomainName(
    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    val id: Int?,
    /** 域名 */
    @TableField("`domain_name`")
    val domainName: String,
    /** 安全状态 */
    @TableField("`state`")
    val state: Int,
) : Serializable
