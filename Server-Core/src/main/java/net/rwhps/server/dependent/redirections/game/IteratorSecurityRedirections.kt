/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.dependent.redirections.game

import net.rwhps.server.dependent.redirections.MainRedirections
import net.rwhps.server.util.inline.accessibleConstructor
import net.rwhps.server.util.inline.findMethod
import net.rwhps.server.util.inline.toClassAutoLoader

@Deprecated("影响效率")
class IteratorSecurityRedirections : MainRedirections {
    override fun register() {
        val listName = "com.corrodinggames.rts.gameFramework.utility.s"
        val listIteratorName = "com.corrodinggames.rts.gameFramework.utility.t"
        redirect(listName,arrayOf("iterator","()Ljava/util/Iterator;")) { obj: Any, _: String?, _: Class<*>?, _: Array<Any?>? ->
            val listClass = listName.toClassAutoLoader(obj)!!
            return@redirect listIteratorName.toClassAutoLoader(obj)!!
                .accessibleConstructor(listClass)
                .newInstance(listClass.findMethod("clone")!!.invoke(obj))
        }
    }
}