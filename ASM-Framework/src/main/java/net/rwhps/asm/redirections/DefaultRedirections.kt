/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.asm.redirections

import net.rwhps.asm.api.Redirection

object DefaultRedirections {
    @JvmField val NULL = Redirection.of(null)!!
    @JvmField val BOOLEANF = Redirection.of(false)!!
    @JvmField val BOOLEANT = Redirection.of(true)!!
    @JvmField val BYTE = Redirection.of(0.toByte())!!
    @JvmField val SHORT = Redirection.of(0.toShort())!!
    @JvmField val INT = Redirection.of(0)!!
    @JvmField val LONG = Redirection.of(0L)!!
    @JvmField val FLOAT = Redirection.of(0.0f)!!
    @JvmField val DOUBLE = Redirection.of(0.0)!!
    @JvmField val CHAR = Redirection.of('a')!!
    @JvmField val STRING = Redirection.of("")!!
    @JvmField val EQUALS = Redirection { _: Any?, _: String?, _: Class<*>?, args: Array<Any> -> args[0] === args[1] }
    @JvmField val HASHCODE = Redirection { _: Any?, _: String?, _: Class<*>?, args: Array<Any?> -> System.identityHashCode(args[0]) }

    private val DEFAULTS = HashMap<Class<*>?, Redirection>().apply {
        this[Void.TYPE] = NULL
        this[Boolean::class.javaPrimitiveType] = BOOLEANF
        this[Byte::class.javaPrimitiveType] = BYTE
        this[Short::class.javaPrimitiveType] = SHORT
        this[Int::class.javaPrimitiveType] = INT
        this[Long::class.javaPrimitiveType] = LONG
        this[Float::class.javaPrimitiveType] = FLOAT
        this[Double::class.javaPrimitiveType] = DOUBLE
        this[Char::class.javaPrimitiveType] = CHAR
        this[String::class.java] = STRING
        this[CharSequence::class.java] = STRING
    }

    @JvmStatic
    fun fallback(type: Class<*>?, redirection: Redirection): Redirection {
        return DEFAULTS.getOrDefault(type, redirection)
    }
}
