/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.asm.redirections

import net.rwhps.asm.api.replace.RedirectionReplace

object DefaultRedirections {
    @JvmField
    val NULL = RedirectionReplace.of(null)

    @JvmField
    val BOOLEANF = RedirectionReplace.of(false)

    @JvmField
    val BOOLEANT = RedirectionReplace.of(true)

    @JvmField
    val BYTE = RedirectionReplace.of(0.toByte())

    @JvmField
    val SHORT = RedirectionReplace.of(0.toShort())

    @JvmField
    val INT = RedirectionReplace.of(0)

    @JvmField
    val LONG = RedirectionReplace.of(0L)

    @JvmField
    val FLOAT = RedirectionReplace.of(0.0f)

    @JvmField
    val DOUBLE = RedirectionReplace.of(0.0)

    @JvmField
    val CHAR = RedirectionReplace.of('a')

    @JvmField
    val STRING = RedirectionReplace.of("")

    @JvmField
    val EQUALS = RedirectionReplace { _: Any, _: String, _: Class<*>, args: Array<out Any?> -> args[0] === args[1] }

    @JvmField
    val HASHCODE = RedirectionReplace { _: Any, _: String, _: Class<*>, args: Array<out Any?> -> System.identityHashCode(args[0]) }

    private val DEFAULTS = HashMap<Class<*>?, RedirectionReplace>().apply {
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
    fun fallback(type: Class<*>?, redirectionReplace: RedirectionReplace): RedirectionReplace {
        return DEFAULTS.getOrDefault(type, redirectionReplace)
    }
}
