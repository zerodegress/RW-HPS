/*
 *
 *  * Copyright 2020-2023 RW-HPS Team and contributors.
 *  *
 *  * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  *
 *  * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 *
 */

package net.rwhps.asm.data

import net.rwhps.asm.api.replace.RedirectionReplace

/**
 * ASM 中需要修改的目标方法信息和需要指向的 Class 方法
 *
 * @date 2023/10/28 12:10
 * @author Dr (dr@der.kim)
 */
data class MethodTypeInfoValue(
    val classPath: String,
    val methodName: String,
    val methodParamsInfo: String,
    val replaceClass: Class<in RedirectionReplace>? = null
) {
    val desc = "L$classPath;${methodName}${methodParamsInfo}"

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other is MethodTypeInfoValue && methodName == other.methodName && methodParamsInfo == other.methodParamsInfo) {
            return false
        }

        return false
    }

    override fun hashCode(): Int {
        var result = methodName.hashCode()
        result = 31 * result + methodParamsInfo.hashCode()
        return result
    }
}
