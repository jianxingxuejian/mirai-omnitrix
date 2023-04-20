package org.hff.miraiomnitrix.common

import com.baomidou.mybatisplus.annotation.IEnum

enum class BooleanEnum(private val value: Int) : IEnum<Int> {
    FALSE(0),
    TRUE(1);

    override fun getValue() = value
}
