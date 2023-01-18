/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.asm.api;

@FunctionalInterface
public interface Redirection {
    String CAST_PREFIX = "<cast> ";
    String METHOD_NAME = "invoke";
    String METHOD_DESC = "(Ljava/lang/Object;Ljava/lang/String;" +
        "Ljava/lang/Class;[Ljava/lang/Object;)Ljava/lang/Object;";

    static Redirection of(Object value) {
        return (obj, desc, type, args) -> value;
    }

    Object invoke(Object obj, String desc, Class<?> type, Object... args) throws Throwable;

}
