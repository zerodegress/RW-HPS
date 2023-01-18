/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.asm.api;

import java.util.function.Supplier;

public interface RedirectionManager extends Redirection {
    Object invoke(String desc, Class<?> type, Object obj,
                  Supplier<Redirection> fallback, Object... args)
        throws Throwable;

    void redirect(String desc, Redirection redirection);

}
