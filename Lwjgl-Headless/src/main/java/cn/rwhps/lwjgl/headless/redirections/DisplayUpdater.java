/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.lwjgl.headless.redirections;

import cn.rwhps.lwjgl.headless.LwjglProperties;
import cn.rwhps.lwjgl.headless.api.Redirection;

/**
 * Since Minecrafts Gameloop is just a while(True) loop which calls {@code
 * org.lwjgl.opengl.Display.update()} and {@link Thread#yield()}. Once we
 * redirect the {@code update()} call the loop just runs and runs and puts some
 * heavy load on the CPU. This {@link Redirection} fixes that by sleeping for a
 * set amount of time, configurable by the SystemProperty {@link
 * LwjglProperties#DISPLAY_UPDATE}.
 */
public class DisplayUpdater implements Redirection {
    public static final String DESC = "Lorg/lwjgl/opengl/Display;update()V";

    private final long time;

    public DisplayUpdater() {
        this(getTime());
    }

    public DisplayUpdater(long time) {
        this.time = time;
    }


    private static long getTime() {
        try {
            return Long.parseLong(
                System.getProperty(LwjglProperties.DISPLAY_UPDATE, "10"));
        } catch (NumberFormatException nfe) {
            return 10L;
        }
    }

    @Override
    public Object invoke(Object obj, String desc, Class<?> type, Object... args)
        throws Throwable {
        // we could scale this with the refresh rate?
        Thread.sleep(time);
        return null;
    }

}
