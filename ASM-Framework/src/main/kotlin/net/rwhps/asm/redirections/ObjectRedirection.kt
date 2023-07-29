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
import net.rwhps.asm.api.RedirectionManager
import net.rwhps.asm.util.DescriptionUtil.getDesc
import java.lang.reflect.Array
import java.lang.reflect.Modifier
import java.lang.reflect.Proxy
import java.util.*

class ObjectRedirection(private val manager: RedirectionManager): Redirection {
    override operator fun invoke(obj: Any, desc: String, type: Class<*>, vararg args: Any?): Any? {
        var typePrivate = type

        return when {
            typePrivate.isInterface -> {
                return Proxy.newProxyInstance(typePrivate.classLoader, arrayOf(typePrivate), ProxyRedirection(manager, getDesc(typePrivate)))
            }
            typePrivate.isArray -> {
                var dimension = 0

                while (typePrivate.isArray) {
                    dimension++
                    typePrivate = typePrivate.componentType
                }

                val dimensions = IntArray(dimension)
                Arrays.fill(dimensions, 0)
                Array.newInstance(typePrivate, *dimensions)
            }
            Modifier.isAbstract(typePrivate.modifiers) -> {
                System.err.println("Can't return abstract class: $desc")
                null
            }
            else -> {
                try {
                    val constructor = typePrivate.getDeclaredConstructor()
                    constructor.isAccessible = true
                    constructor.newInstance()
                } catch (e: SecurityException) {
                    e.printStackTrace()
                    null
                } catch (e: ReflectiveOperationException) {
                    e.printStackTrace()
                    null
                }
            }
        }
    }
}
