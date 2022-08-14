package org.hff.miraiomnitrix.app.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import java.io.Serializable
import java.math.BigDecimal

class Bgm : Serializable {

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    var id: Int? = null

    /** 中文名 */
    var name: String? = null

    /** 原名 */
    var nameOriginal: String? = null

    /** 排名 */
    var rank: Short? = null

    /** 年份 */
    var year: Short? = null

    /** 图片url */
    var imgUrl: String? = null

    /** 评分 */
    var rate: BigDecimal? = null

    /** 评分人数 */
    var rateNum: Short? = null

    /** 说明 */
    var info: String? = null
}
