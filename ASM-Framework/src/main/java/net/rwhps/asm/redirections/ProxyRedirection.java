/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.asm.redirections;

import net.rwhps.asm.api.Redirection;
import net.rwhps.asm.api.RedirectionManager;
import net.rwhps.asm.util.DescriptionUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.function.Supplier;

public class ProxyRedirection implements InvocationHandler {
    private final RedirectionManager manager;
    private final String internalName;

    public ProxyRedirection(@NotNull RedirectionManager manager, @NotNull String internalName) {
        this.manager = manager;
        this.internalName = internalName;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] argsIn)
        throws Throwable {
        String desc = internalName + DescriptionUtil.getDesc(method);
        Supplier<Redirection> fb = () -> manager;
        if (desc.endsWith(";equals(Ljava/lang/Object;)Z")) {
            fb = () -> DefaultRedirections.EQUALS;
        } else if (desc.endsWith(";hashCode()I")) {
            fb = () -> DefaultRedirections.HASHCODE;
        }

        Object[] args = new Object[argsIn == null ? 1 : argsIn.length + 1];
        // there's basically no way the method is static
        args[0] = proxy;
        if (argsIn != null) {
            System.arraycopy(argsIn, 0, args, 1, argsIn.length);
        }
        return manager.invoke(desc, method.getReturnType(), proxy, fb, args);
    }

}
