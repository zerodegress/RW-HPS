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
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.util.function.Supplier

class ProxyRedirection(private val manager: RedirectionManager, private val internalName: String): InvocationHandler {
    @Throws(Throwable::class)
    override fun invoke(proxy: Any, method: Method, argsIn: Array<Any>?): Any {
        val desc = internalName + getDesc(method)
        var fb = Supplier<Redirection> { manager }

        if (desc.endsWith(";equals(Ljava/lang/Object;)Z")) {
            fb = Supplier { DefaultRedirections.EQUALS }
        } else if (desc.endsWith(";hashCode()I")) {
            fb = Supplier { DefaultRedirections.HASHCODE }
        }
        val args = arrayOfNulls<Any>(if (argsIn == null) 1 else argsIn.size + 1)

        // there's basically no way the method is static
        args[0] = proxy
        if (argsIn != null) {
            System.arraycopy(argsIn, 0, args, 1, argsIn.size)
        }
        return manager.invoke(desc, method.returnType, proxy, fb, *args)!!
    }
}
