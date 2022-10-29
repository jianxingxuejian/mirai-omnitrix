package org.hff.miraiomnitrix.app.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.io.Serializable

@TableName("`character`")
class Character: Serializable {

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    var id: Int? = null

    /** 角色名 */
    @TableField("`name`")
    var name: String? = null

    var externalId: String? = null
}