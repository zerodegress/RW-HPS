/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.dependent.redirections.game

import net.rwhps.server.data.global.Data
import net.rwhps.server.dependent.redirections.MainRedirections

class CustomRedirections : MainRedirections {
    override fun register() {
        redirect("com/corrodinggames/rts/gameFramework/j/ad", arrayOf("Z","()Ljava/lang/String;")) { obj: Any, _: String?, _: Class<*>?, _: Array<Any?>? ->
            return@redirect Data.core.serverHessUuid
        }
    }
}