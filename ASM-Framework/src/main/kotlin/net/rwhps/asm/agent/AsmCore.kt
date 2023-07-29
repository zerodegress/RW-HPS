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
import java.security.ProtectionDomain

open class AsmCore {
    protected val setAgent = { agent: AsmAgent -> AsmCore.agent = agent }

    companion object {
        private val allIgnore0 = ArrayList<Find<String, Boolean>>()
        private val partialMethod0 = HashMap<String, ArrayList<Array<String>>>()
        val allMethod = ArrayList<String>()
        private var agent: AsmAgent? = null

        @JvmStatic
        fun transform(loader: ClassLoader?, className: String, classfileBuffer: ByteArray): ByteArray {
            return transform(loader, className, null, null, classfileBuffer)
        }

        @JvmStatic
        fun transform(
            loader: ClassLoader?,
            className: String,
            classBeingRedefined: Class<*>?,
            protectionDomain: ProtectionDomain?,
            classfileBuffer: ByteArray
        ): ByteArray {
            return agent?.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer) ?: classfileBuffer
        }
    }
}