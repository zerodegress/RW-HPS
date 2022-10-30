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
import cn.rwhps.lwjgl.headless.api.RedirectionManager;

import java.lang.reflect.Field;
import java.nio.*;
import java.util.HashMap;

// TODO: redirect Keyboard and Mouse?
public class LwjglRedirections {
    public static final int TEXTURE_SIZE = Integer.parseInt(
        System.getProperty(LwjglProperties.TEXTURE_SIZE, "1024"));
    public static final boolean FULLSCREEN = Boolean.parseBoolean(
        System.getProperty(LwjglProperties.FULLSCREEN, "true"));
    public static final int SCREEN_WIDTH = Integer.parseInt(
        System.getProperty(LwjglProperties.SCREEN_WIDTH, "1920"));
    public static final int SCREEN_HEIGHT = Integer.parseInt(
        System.getProperty(LwjglProperties.SCREEN_HEIGHT, "1080"));
    public static final int REFRESH_RATE = Integer.parseInt(
        System.getProperty(LwjglProperties.REFRESH_RATE, "100"));
    public static final int BITS_PER_PIXEL = Integer.parseInt(
        System.getProperty(LwjglProperties.BITS_PER_PIXEL, "32"));
    public static final int JNI_VERSION = Integer.parseInt(
        System.getProperty(LwjglProperties.JNI_VERSION, "24"));
    private static final ThreadLocal<Long> CURRENT_BUFFER_SIZE =
        ThreadLocal.withInitial(() -> 0L);
    private static final long START = System.nanoTime();

    public static HashMap<String, Redirection> customRedirection = new HashMap();

    public static void register(RedirectionManager manager) {
        customRedirection.forEach(manager::redirect);

        manager.redirect(DisplayUpdater.DESC, new DisplayUpdater());
        manager.redirect("Lorg/lwjgl/opengl/Display;isCreated()Z", Redirection.of(true));

        manager.redirect("Lorg/lwjgl/glfw/GLFW;glfwWaitEventsTimeout(D)V",
                         (obj, desc, type, args) -> {
                             Thread.sleep((long) ((double) args[0] * 1000L));
                             return null;
                         });

        // 1.16.5-forge-36.2.22, ClientVisualization
        manager.redirect("Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(" +
                             "IILjava/lang/CharSequence;JJ)J", Redirection.of(1L));

        // 1.16.5-forge-36.2.22, GlStateManager
        manager.redirect(
            "Lorg/lwjgl/opengl/GLCapabilities;<init>()V",
            (obj, desc, type, args) -> {
                try {
                    Field field = obj.getClass().getDeclaredField("OpenGL30");
                    field.setAccessible(true);
                    field.set(obj, true);
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }

                return null;
            });

        manager.redirect("Lorg/lwjgl/glfw/GLFW;glfwGetTime()D",
                         (obj, desc, type, args) ->
                             (System.nanoTime() - START) / 1_000_000_000.0D);

        // TODO: check this does what it's supposed to
        manager.redirect("Lorg/lwjgl/glfw/GLFW;glfwGetFramebufferSize(J[I[I)V",
                         (obj, desc, type, args) -> {
                             int[] width = (int[]) args[1];
                             width[0] = SCREEN_WIDTH;
                             int[] height = (int[]) args[2];
                             height[0] = SCREEN_HEIGHT;
                             return null;
                         });

        manager.redirect("Lorg/lwjgl/opengl/Display;getWidth()I",
                         Redirection.of(SCREEN_WIDTH));
        manager.redirect("Lorg/lwjgl/opengl/Display;getHeight()I",
                         Redirection.of(SCREEN_HEIGHT));
        manager.redirect("Lorg/lwjgl/opengl/Display;isFullscreen()Z",
                         Redirection.of(FULLSCREEN));

        manager.redirect("Lorg/lwjgl/DefaultSysImplementation;getJNIVersion()I",
                         Redirection.of(JNI_VERSION));

        // TODO: make this configurable?
        manager.redirect("Lorg/lwjgl/opengl/Display;isActive()Z", Redirection.of(true));

        manager.redirect("Lorg/lwjgl/opengl/DisplayMode;isFullscreenCapable()Z",
                         Redirection.of(FULLSCREEN));
        manager.redirect("Lorg/lwjgl/opengl/DisplayMode;getWidth()I",
                         Redirection.of(SCREEN_WIDTH));
        manager.redirect("Lorg/lwjgl/opengl/DisplayMode;getHeight()I",
                         Redirection.of(SCREEN_HEIGHT));
        manager.redirect("Lorg/lwjgl/opengl/DisplayMode;getFrequency()I",
                         Redirection.of(REFRESH_RATE));
        manager.redirect("Lorg/lwjgl/opengl/DisplayMode;getBitsPerPixel()I",
                         Redirection.of(BITS_PER_PIXEL));

        manager.redirect("Lorg/lwjgl/glfw/GLFW;glfwInit()Z",
                         Redirection.of(true));
        manager.redirect("Lorg/lwjgl/Sys;getVersion()Ljava/lang/String;",
                         Redirection.of("RW-HPS-Headless-Lwjgl"));

        manager.redirect("Lorg/lwjgl/Sys;getTimerResolution()J",
                         Redirection.of(1000L));
        manager.redirect("Lorg/lwjgl/Sys;getTime()J", (obj, desc, type, args)
            -> System.nanoTime() / 1000000L);

        manager.redirect("Lorg/lwjgl/opengl/GL11;glGetTexLevelParameteri(III)I",
                         Redirection.of(TEXTURE_SIZE));
        manager.redirect("Lorg/lwjgl/opengl/GL11;glGenLists(I)I", Redirection.of(-1));

        manager.redirect(
            "Lorg/lwjgl/system/MemoryUtil$MemoryAllocator;malloc(J)J",
            Redirection.of(1L));
        manager.redirect(
            "Lorg/lwjgl/system/MemoryUtil$MemoryAllocator;realloc(J)J",
            Redirection.of(1L));
        manager.redirect(
            "Lorg/lwjgl/system/MemoryUtil$MemoryAllocator;calloc(J)J",
            Redirection.of(1L));
        manager.redirect(
            "Lorg/lwjgl/system/MemoryUtil$MemoryAllocator;realloc(J)J",
            Redirection.of(1L));
        manager.redirect(
            "Lorg/lwjgl/system/MemoryUtil$MemoryAllocator;realloc(JJ)J",
            Redirection.of(1L));
        manager.redirect(
            "Lorg/lwjgl/system/MemoryUtil$MemoryAllocator;aligned_alloc(JJ)J",
            Redirection.of(1L));

        // blaze3d RenderTarget
        manager.redirect("Lorg/lwjgl/opengl/GL30;glCheckFramebufferStatus(I)I",
                         Redirection.of(36053));

        // blaze3d NativeImage
        manager.redirect("Lorg/lwjgl/system/MemoryUtil;nmemAlloc(J)J",
                         Redirection.of(1L));

        // TODO: because MemoryUtil and the Buffers are actually being used,
        //  redirect all methods inside those to return proper Buffers?
        //  - ignore list?
        // I WISH WE COULD SUBCLASS BUFFERS WTF
        manager.redirect("Lorg/lwjgl/system/MemoryUtil;memByteBuffer(JI)" +
                             "Ljava/nio/ByteBuffer;",
                         (obj, desc, type, args) ->
                             ByteBuffer.wrap(new byte[(int) args[1]]));
        manager.redirect("Lorg/lwjgl/system/MemoryUtil;" +
                             "memAlloc(I)Ljava/nio/ByteBuffer;",
                         (obj, desc, type, args) -> ByteBuffer.wrap(
                             new byte[(int) args[0]]));
        manager.redirect("Lorg/lwjgl/system/MemoryUtil;memRealloc" +
                             "(Ljava/nio/ByteBuffer;I)Ljava/nio/ByteBuffer;",
                         (obj, desc, type, args) -> ByteBuffer.wrap(
                             new byte[(int) args[1]]));
        manager.redirect("Lorg/lwjgl/system/MemoryStack;" +
                             "mallocInt(I)Ljava/nio/IntBuffer;",
                         (obj, desc, type, args) -> IntBuffer.wrap(
                             new int[(int) args[0]]));
        manager.redirect("Lorg/lwjgl/BufferUtils;createIntBuffer(I)" +
                             "Ljava/nio/IntBuffer;",
                         (obj, desc, type, args) -> IntBuffer.wrap(
                             new int[(int) args[0]]));
        manager.redirect("Lorg/lwjgl/BufferUtils;createFloatBuffer(I)" +
                             "Ljava/nio/FloatBuffer;",
                         (obj, desc, type, args) -> FloatBuffer.wrap(
                             new float[(int) args[0]]));
        manager.redirect("Lorg/lwjgl/system/MemoryUtil;createIntBuffer(I)" +
                             "Ljava/nio/IntBuffer;",
                         (obj, desc, type, args) -> IntBuffer.wrap(
                             new int[(int) args[0]]));
        manager.redirect("Lorg/lwjgl/system/MemoryUtil;" +
                             "memAllocFloat(I)Ljava/nio/FloatBuffer;",
                         (obj, desc, type, args) -> FloatBuffer.wrap(
                             new float[(int) args[0]]));
        manager.redirect("Lorg/lwjgl/BufferUtils;createByteBuffer(I)" +
                             "Ljava/nio/ByteBuffer;",
                         (obj, desc, type, args) -> ByteBuffer.wrap(
                             new byte[(int) args[0]]));
        manager.redirect("Lorg/lwjgl/BufferUtils;createDoubleBuffer(I)" +
                        "Ljava/nio/DoubleBuffer;",
                (obj, desc, type, args) -> DoubleBuffer.wrap(
                        new double[(int) args[0]]));
        manager.redirect("Lorg/lwjgl/system/MemoryUtil;memAllocInt(I)" +
                             "Ljava/nio/IntBuffer;",
                         (obj, desc, type, args) -> IntBuffer.wrap(
                             new int[(int) args[0] << 2]));
        manager.redirect("Lorg/lwjgl/system/MemoryUtil;memAllocLong(I)" +
                             "Ljava/nio/LongBuffer;",
                         (obj, desc, type, args) -> LongBuffer.wrap(
                             new long[(int) args[0]]));
        manager.redirect("Lorg/lwjgl/system/MemoryUtil;memAllocDouble(I)" +
                             "Ljava/nio/DoubleBuffer;",
                         (obj, desc, type, args) -> DoubleBuffer.wrap(
                             new double[(int) args[0]]));
        manager.redirect("Lorg/lwjgl/system/MemoryUtil;memAllocShort(I)" +
                             "Ljava/nio/ShortBuffer;",
                         (obj, desc, type, args) -> ShortBuffer.wrap(
                             new short[(int) args[0]]));
        manager.redirect("Lorg/lwjgl/system/MemoryStack;malloc(I)" +
                             "Ljava/nio/ByteBuffer;",
                         (obj, desc, type, args) -> ByteBuffer.wrap(
                             new byte[(int) args[0]]));

        // TODO: this is really bad...
        manager.redirect("Lorg/lwjgl/opengl/GL15;glBufferData(IJI)V",
                         (obj, desc, type, args) -> {
                             CURRENT_BUFFER_SIZE.set((Long) args[1]);
                             return null;
                         });
        manager.redirect("Lorg/lwjgl/opengl/GL15;glMapBuffer(II)" +
                             "Ljava/nio/ByteBuffer;",
                         (obj, desc, type, args) -> ByteBuffer.wrap(
                             new byte[CURRENT_BUFFER_SIZE.get().intValue()]));

        manager.redirect("Lorg/lwjgl/system/MemoryUtil;" +
                             "memAddress(Ljava/nio/ByteBuffer;)J", Redirection.of(1L));

        manager.redirect(STBIImageRedirection.DESC,
                         STBIImageRedirection.INSTANCE);
        manager.redirect(MemASCIIRedirection.DESC,
                         MemASCIIRedirection.INSTANCE);

        // act as if we compiled a shader program (blaze3d program)
        manager.redirect("Lorg/lwjgl/opengl/GL20;glGetShaderi(II)I", Redirection.of(1));
        manager.redirect("Lorg/lwjgl/opengl/GL20;glCreateProgram()I", Redirection.of(1));
        manager.redirect("Lorg/lwjgl/opengl/GL20;glGetProgrami(II)I", Redirection.of(1));

        manager.redirect("Lorg/lwjgl/openal/ALC10;alcOpenDevice(" +
                             "Ljava/lang/CharSequence;)J", Redirection.of(1L));


        manager.redirect("Lorg/lwjgl/system/MemoryUtil;memSlice(" +
                             "Ljava/nio/ByteBuffer;II)Ljava/nio/ByteBuffer;",
                         (obj, desc, type, args) -> {
                             // TODO: redirected for now to prevent console
                             //  spam, but this should be done properly at some
                             //  point. It looks as if the returned buffer is
                             //  never used except for other redirected lwjgl
                             //  methods, so null is fairly safe rn.
                             /*
                             ByteBuffer buffer = (ByteBuffer) args[0];
                             if (buffer == null) {
                                return null;
                             }

                             int offset = (int) args[1];
                             int capacity = (int) args[2];

                             int position = buffer.position() + offset;
                             if (offset < 0 || buffer.limit() < position) {
                                 throw new IllegalArgumentException();
                             }

                             if (capacity < 0 || buffer.capacity()
                                 - position < capacity) {
                                 throw new IllegalArgumentException();
                             }

                             // TODO: allocate new bytebuffer of size and
                             //       copy bytes to that buffer

                             */

                             return null;
                         });
    }

}
