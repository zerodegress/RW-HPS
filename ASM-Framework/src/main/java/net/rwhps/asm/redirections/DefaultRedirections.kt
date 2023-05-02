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

    private val DEFAULTS: MutableMap<Class<*>?, Redirection> = HashMap()

    init {
        DEFAULTS[Void.TYPE] = NULL
        DEFAULTS[Boolean::class.javaPrimitiveType] = BOOLEANF
        DEFAULTS[Byte::class.javaPrimitiveType] = BYTE
        DEFAULTS[Short::class.javaPrimitiveType] = SHORT
        DEFAULTS[Int::class.javaPrimitiveType] = INT
        DEFAULTS[Long::class.javaPrimitiveType] = LONG
        DEFAULTS[Float::class.javaPrimitiveType] = FLOAT
        DEFAULTS[Double::class.javaPrimitiveType] = DOUBLE
        DEFAULTS[Char::class.javaPrimitiveType] = CHAR
        DEFAULTS[String::class.java] = STRING
        DEFAULTS[CharSequence::class.java] = STRING
    }

    @JvmStatic
    fun fallback(type: Class<*>?, redirection: Redirection): Redirection {
        return DEFAULTS.getOrDefault(type, redirection)
    }
}
