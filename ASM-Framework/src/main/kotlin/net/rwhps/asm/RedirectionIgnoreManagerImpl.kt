/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.asm

import net.rwhps.asm.api.replace.RedirectionReplace
import net.rwhps.asm.api.replace.RedirectionReplaceManager
import net.rwhps.asm.redirections.DefaultRedirections.fallback
import net.rwhps.asm.redirections.replace.CastRedirectionReplace
import net.rwhps.asm.redirections.replace.ObjectRedirectionReplace
import java.util.function.Supplier

class RedirectionIgnoreManagerImpl: RedirectionReplaceManager {
    private val objectRedirectionListener: RedirectionReplace = ObjectRedirectionReplace(this)
    private val cast: RedirectionReplace = CastRedirectionReplace(this)

    @Throws(Throwable::class)
    override operator fun invoke(obj: Any, desc: String, type: Class<*>, vararg args: Any?): Any? {
        return invoke(desc, type, obj, { getFallback(desc, type) }, *args)
    }

    @Throws(Throwable::class)
    override fun invoke(desc: String, type: Class<*>, obj: Any, fallback: Supplier<RedirectionReplace>, vararg args: Any?): Any? {
        return fallback.get().invoke(obj, desc, type, *args)
    }

    private fun getFallback(desc: String, type: Class<*>): RedirectionReplace {
        return if (desc.startsWith(RedirectionReplace.CAST_PREFIX)) {
            // TODO: currently cast redirection looks like this:
            //  <cast> java/lang/String
            //  <init> <cast> java/lang/String
            //  It contains no information about the calling class!
            cast
        } else {
            fallback(type, objectRedirectionListener)
        }
    }
}
