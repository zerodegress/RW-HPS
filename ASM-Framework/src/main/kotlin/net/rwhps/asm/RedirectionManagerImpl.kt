/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.asm

import net.rwhps.asm.api.Redirection
import net.rwhps.asm.api.RedirectionManager
import net.rwhps.asm.redirections.AsmRedirections.register
import net.rwhps.asm.redirections.CastRedirection
import net.rwhps.asm.redirections.DefaultRedirections.fallback
import net.rwhps.asm.redirections.ObjectRedirection
import java.util.function.Supplier

class RedirectionManagerImpl: RedirectionManager {
    private val redirects: MutableMap<String, Redirection> = HashMap()
    private val objectRedirection: Redirection = ObjectRedirection(this)
    private val cast: Redirection = CastRedirection(this)

    init {
        register(this)
    }

    override fun redirect(desc: String, redirection: Redirection) {
        redirects[desc] = redirection
    }

    @Throws(Throwable::class)
    override operator fun invoke(obj: Any, desc: String, type: Class<*>, vararg args: Any?): Any? {
        return invoke(desc, type, obj, { getFallback(desc, type) }, *args)
    }

    @Throws(Throwable::class)
    override fun invoke(desc: String, type: Class<*>, obj: Any, fallback: Supplier<Redirection>, vararg args: Any?): Any? {
        var redirection = redirects[desc]
        if (redirection == null) {
            redirection = fallback.get()
        }
        return redirection.invoke(obj, desc, type, *args)
    }

    private fun getFallback(desc: String, type: Class<*>): Redirection {
        return if (desc.startsWith(Redirection.CAST_PREFIX)) {
            // TODO: currently cast redirection looks like this:
            //  <cast> java/lang/String
            //  <init> <cast> java/lang/String
            //  It contains no information about the calling class!
            cast
        } else {
            fallback(type, objectRedirection)
        }
    }
}
