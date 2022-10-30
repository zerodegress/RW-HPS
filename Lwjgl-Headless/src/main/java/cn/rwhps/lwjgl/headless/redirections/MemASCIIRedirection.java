/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.lwjgl.headless.redirections;

import cn.rwhps.lwjgl.headless.api.Redirection;

import java.nio.ByteBuffer;

public enum MemASCIIRedirection implements Redirection {
    INSTANCE;

    public static final String DESC = "Lorg/lwjgl/system/MemoryUtil;" +
        "memASCII(Ljava/nio/ByteBuffer;I)Ljava/lang/String;";

    @Override
    public Object invoke(Object obj, String desc, Class<?> type, Object... args)
        throws Throwable {
        ByteBuffer buffer = (ByteBuffer) args[0];
        int length = (int) args[1];
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) buffer.get());
        }

        return sb.toString();
    }

}
