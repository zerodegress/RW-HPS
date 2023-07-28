/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.asm.agent

import net.rwhps.asm.api.Redirection
import net.rwhps.asm.func.Find
import net.rwhps.asm.redirections.AsmRedirections
import net.rwhps.asm.redirections.DefaultRedirections

/**
 * @date 2023/7/27 17:46
 * @author RW-HPS/Dr
 */
object AsmData {
    internal val allIgnore0 = ArrayList<Find<String, Boolean>>()
    internal val partialMethod0 = HashMap<String, ArrayList<Array<String>>>()

    @JvmStatic
    fun allIgnore(classFind: Find<String, Boolean>) {
        allIgnore0.add(classFind)
    }

    /**
     * 加入 指定 Class 的指定方法代理
     *
     *
     * @param desc String
     * @param p Array<String>
     * @param redirection Redirection
     */
    @JvmStatic
    @JvmOverloads
    fun addPartialMethod(desc: String, p: Array<String>, redirection: Redirection = DefaultRedirections.NULL) {
        if (partialMethod0.containsKey(desc)) {
            partialMethod0[desc]!!.add(p)
        } else {
            partialMethod0[desc] = ArrayList<Array<String>>().also { it.add(p) }
        }

        // 这里构造 Desc 并且 加入对应的 取代
        AsmRedirections.redirect("L" + desc + ";" + p[0] + p[1], redirection)
    }
}