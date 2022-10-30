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
import cn.rwhps.lwjgl.headless.util.ByteBufferInputStream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public enum STBIImageRedirection implements Redirection {
    INSTANCE;

    public static final String DESC =
        "Lorg/lwjgl/stb/STBImage;stbi_load_from_memory(" +
            "Ljava/nio/ByteBuffer;Ljava/nio/IntBuffer;" +
            "Ljava/nio/IntBuffer;Ljava/nio/IntBuffer;" +
            "I)Ljava/nio/ByteBuffer;";
    private static final BufferedImage DUMMY =
        new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

    @Override
    public Object invoke(Object obj, String desc, Class<?> type, Object... args)
        throws Throwable {
        ByteBuffer buffer = (ByteBuffer) args[0];
        IntBuffer x = (IntBuffer) args[1];
        IntBuffer y = (IntBuffer) args[2];
        IntBuffer channels_in_file = (IntBuffer) args[3];
        int desired_channels = (int) args[4];

        ByteBuffer result = ByteBuffer.wrap(
            new byte[x.get(x.position()) * y.get(y.position())
                * (desired_channels != 0
                ? desired_channels
                : channels_in_file.get(channels_in_file.position()))]);

        BufferedImage image = readImage(buffer);
        x.put(0, image.getWidth());
        y.put(0, image.getHeight());
        // TODO: check discrepancies between desired_channels and actual
        channels_in_file.put(0, desired_channels);
        return result;
    }

    private BufferedImage readImage(ByteBuffer buffer) {
        int position = buffer.position();
        BufferedImage image = null;
        try {
            image = ImageIO.read(new ByteBufferInputStream(buffer));
        } catch (IOException e) {
            e.printStackTrace();
        }

        buffer.position(position);
        return image == null ? DUMMY : image;
    }

}
