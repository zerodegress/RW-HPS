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
import org.jetbrains.annotations.NotNull;

public class CastRedirection implements Redirection {
    private final RedirectionManager manager;

    public CastRedirection(@NotNull RedirectionManager manager) {
        this.manager = manager;
    }

    @Override
    public Object invoke(Object obj, String desc, Class<?> type, Object... args)
        throws Throwable {
        if (obj == null || type.isInstance(obj)) {
            return obj;
        }

        return manager.invoke(obj, "<init> " + desc, type, args);
    }

}
