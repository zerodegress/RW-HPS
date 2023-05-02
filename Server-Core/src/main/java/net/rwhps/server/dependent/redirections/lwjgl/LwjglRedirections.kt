package net.rwhps.server.dependent.redirections.lwjgl

import net.rwhps.asm.agent.AsmCore
import net.rwhps.asm.api.Redirection
import net.rwhps.asm.redirections.DefaultRedirections
import net.rwhps.server.dependent.redirections.MainRedirections
import net.rwhps.server.dependent.redirections.slick.AppGameContainerUpdate
import net.rwhps.server.util.alone.annotations.AsmMark
import net.rwhps.server.util.alone.annotations.NeedHelp
import java.nio.*

@NeedHelp(info_EN = "redirect Keyboard and Mouse")
@AsmMark.ClassLoaderCompatible
class LwjglRedirections : MainRedirections {
    private val textureSize = System.getProperty(LwjglClassProperties.TEXTURE_SIZE, "1024").toInt()
    private val fullScreen = System.getProperty(LwjglClassProperties.FULLSCREEN, "true").toBoolean()
    private val screenWidth = System.getProperty(LwjglClassProperties.SCREEN_WIDTH, "1920").toInt()
    private val screenHeight = System.getProperty(LwjglClassProperties.SCREEN_HEIGHT, "1080").toInt()
    private val refreshRate = System.getProperty(LwjglClassProperties.REFRESH_RATE, "100").toInt()
    private val bitsPerPixel = System.getProperty(LwjglClassProperties.BITS_PER_PIXEL, "32").toInt()
    private val jniVersion = System.getProperty(LwjglClassProperties.JNI_VERSION, "24").toInt()
    
    private val currentBufferSize = ThreadLocal.withInitial { 0L }
    private val startTime = System.nanoTime()


    override fun register() {
        // 覆写 lwjgl 并跳过 RW-HPS 包
        AsmCore.allIgnore { className ->
            return@allIgnore className.contains("lwjgl") && !className.contains("rwhps")
        }

        redirect(AppGameContainerUpdate.DESC, AppGameContainerUpdate())

        redirect("Lorg/lwjgl/opengl/Display;isCreated()Z", Redirection.of(true))
        redirect("Lorg/lwjgl/opengl/Display;isVisible()Z", Redirection.of(true))
        redirect("Lorg/lwjgl/glfw/GLFW;glfwWaitEventsTimeout(D)V") { _: Any?, _: String?, _: Class<*>?, args: Array<Any> ->
            Thread.sleep((args[0] as Double * 1000L).toLong())
            null
        }

        redirect("Lorg/lwjgl/glfw/GLFW;glfwGetTime()D") { _: Any?, _: String?, _: Class<*>?, _: Array<Any?>? ->
            (System.nanoTime() - startTime) / 1000000000.0
        }

        // TODO: check this does what it's supposed to
        redirect("Lorg/lwjgl/glfw/GLFW;glfwGetFramebufferSize(J[I[I)V") { _: Any?, _: String?, _: Class<*>?, args: Array<Any> ->
            val width = args[1] as IntArray
            width[0] = screenWidth
            val height = args[2] as IntArray
            height[0] = screenHeight
            null
        }

        redirect("Lorg/lwjgl/opengl/Display;getWidth()I", Redirection.of(screenWidth))
        redirect("Lorg/lwjgl/opengl/Display;getHeight()I", Redirection.of(screenHeight))
        redirect("Lorg/lwjgl/opengl/Display;isFullscreen()Z", Redirection.of(fullScreen))
        redirect("Lorg/lwjgl/DefaultSysImplementation;getJNIVersion()I", Redirection.of(jniVersion))

        // TODO: make this configurable?
        redirect("Lorg/lwjgl/opengl/Display;isActive()Z", Redirection.of(true))
        redirect("Lorg/lwjgl/opengl/DisplayMode;isFullscreenCapable()Z", Redirection.of(fullScreen))
        redirect("Lorg/lwjgl/opengl/DisplayMode;getWidth()I", Redirection.of(screenWidth))
        redirect("Lorg/lwjgl/opengl/DisplayMode;getHeight()I", Redirection.of(screenHeight))
        redirect("Lorg/lwjgl/opengl/DisplayMode;getFrequency()I", Redirection.of(refreshRate))
        redirect("Lorg/lwjgl/opengl/DisplayMode;getBitsPerPixel()I", Redirection.of(bitsPerPixel))
        redirect("Lorg/lwjgl/glfw/GLFW;glfwInit()Z", Redirection.of(true))
        redirect("Lorg/lwjgl/Sys;getVersion()Ljava/lang/String;", Redirection.of("RW-HPS-Headless-Lwjgl"))
        redirect("Lorg/lwjgl/Sys;getTimerResolution()J", Redirection.of(1000L))
        redirect("Lorg/lwjgl/Sys;getTime()J") { _: Any?, _: String?, _: Class<*>?, _: Array<Any?>? -> System.nanoTime() / 1000000L }
        redirect("Lorg/lwjgl/opengl/GL11;glGetTexLevelParameteri(III)I", Redirection.of(textureSize))
        redirect("Lorg/lwjgl/opengl/GL11;glGenLists(I)I", Redirection.of(-1))
        redirect("Lorg/lwjgl/system/MemoryUtil\$MemoryAllocator;malloc(J)J", Redirection.of(1L))
        redirect("Lorg/lwjgl/system/MemoryUtil\$MemoryAllocator;realloc(J)J", Redirection.of(1L))
        redirect("Lorg/lwjgl/system/MemoryUtil\$MemoryAllocator;calloc(J)J", Redirection.of(1L))
        redirect("Lorg/lwjgl/system/MemoryUtil\$MemoryAllocator;realloc(J)J", Redirection.of(1L))
        redirect("Lorg/lwjgl/system/MemoryUtil\$MemoryAllocator;realloc(JJ)J", Redirection.of(1L))
        redirect("Lorg/lwjgl/system/MemoryUtil\$MemoryAllocator;aligned_alloc(JJ)J", Redirection.of(1L))

        // blaze3d RenderTarget
        redirect("Lorg/lwjgl/opengl/GL30;glCheckFramebufferStatus(I)I", Redirection.of(36053))

        // blaze3d NativeImage
        redirect("Lorg/lwjgl/system/MemoryUtil;nmemAlloc(J)J", Redirection.of(1L))

        // TODO: because MemoryUtil and the Buffers are actually being used,
        //  redirect all methods inside those to return proper Buffers?
        //  - ignore list?
        // I WISH WE COULD SUBCLASS BUFFERS WTF
        redirect("Lorg/lwjgl/system/MemoryUtil;memByteBuffer(JI)" + "Ljava/nio/ByteBuffer;") { _: Any?, _: String?, _: Class<*>?, args: Array<Any> ->
            ByteBuffer.wrap(ByteArray(args[1] as Int))
        }

        redirect("Lorg/lwjgl/system/MemoryUtil;" +"memAlloc(I)Ljava/nio/ByteBuffer;") { _: Any?, _: String?, _: Class<*>?, args: Array<Any> ->
            ByteBuffer.wrap(ByteArray(args[0] as Int))
        }
        redirect("Lorg/lwjgl/system/MemoryUtil;memRealloc" + "(Ljava/nio/ByteBuffer;I)Ljava/nio/ByteBuffer;") { _: Any?, _: String?, _: Class<*>?, args: Array<Any> ->
            ByteBuffer.wrap(ByteArray(args[1] as Int))
        }
        redirect("Lorg/lwjgl/system/MemoryStack;" + "mallocInt(I)Ljava/nio/IntBuffer;") { _: Any?, _: String?, _: Class<*>?, args: Array<Any> ->
            IntBuffer.wrap(IntArray(args[0] as Int))
        }
        redirect("Lorg/lwjgl/BufferUtils;createIntBuffer(I)" + "Ljava/nio/IntBuffer;") { _: Any?, _: String?, _: Class<*>?, args: Array<Any> ->
            IntBuffer.wrap(IntArray(args[0] as Int))
        }
        redirect("Lorg/lwjgl/BufferUtils;createFloatBuffer(I)" + "Ljava/nio/FloatBuffer;") { _: Any?, _: String?, _: Class<*>?, args: Array<Any> ->
            FloatBuffer.wrap(FloatArray(args[0] as Int))
        }
        redirect("Lorg/lwjgl/system/MemoryUtil;createIntBuffer(I)" + "Ljava/nio/IntBuffer;") { _: Any?, _: String?, _: Class<*>?, args: Array<Any> ->
            IntBuffer.wrap(IntArray(args[0] as Int))
        }
        redirect("Lorg/lwjgl/system/MemoryUtil;" + "memAllocFloat(I)Ljava/nio/FloatBuffer;") { _: Any?, _: String?, _: Class<*>?, args: Array<Any> ->
            FloatBuffer.wrap(FloatArray(args[0] as Int))
        }
        redirect("Lorg/lwjgl/BufferUtils;createByteBuffer(I)" + "Ljava/nio/ByteBuffer;") { _: Any?, _: String?, _: Class<*>?, args: Array<Any> ->
            ByteBuffer.wrap(ByteArray(args[0] as Int))
        }
        redirect("Lorg/lwjgl/BufferUtils;createDoubleBuffer(I)" + "Ljava/nio/DoubleBuffer;") { _: Any?, _: String?, _: Class<*>?, args: Array<Any> ->
            DoubleBuffer.wrap(DoubleArray(args[0] as Int))
        }
        redirect("Lorg/lwjgl/system/MemoryUtil;memAllocInt(I)" + "Ljava/nio/IntBuffer;") { _: Any?, _: String?, _: Class<*>?, args: Array<Any> ->
            IntBuffer.wrap(IntArray(args[0] as Int shl 2))
        }
        redirect("Lorg/lwjgl/system/MemoryUtil;memAllocLong(I)" + "Ljava/nio/LongBuffer;") { _: Any?, _: String?, _: Class<*>?, args: Array<Any> ->
            LongBuffer.wrap(LongArray(args[0] as Int))
        }
        redirect("Lorg/lwjgl/system/MemoryUtil;memAllocDouble(I)" + "Ljava/nio/DoubleBuffer;") { _: Any?, _: String?, _: Class<*>?, args: Array<Any> ->
            DoubleBuffer.wrap(DoubleArray(args[0] as Int))
        }
        redirect("Lorg/lwjgl/system/MemoryUtil;memAllocShort(I)" + "Ljava/nio/ShortBuffer;") { _: Any?, _: String?, _: Class<*>?, args: Array<Any> ->
            ShortBuffer.wrap(ShortArray(args[0] as Int))
        }
        redirect("Lorg/lwjgl/system/MemoryStack;malloc(I)" + "Ljava/nio/ByteBuffer;") { _: Any?, _: String?, _: Class<*>?, args: Array<Any> ->
            ByteBuffer.wrap(ByteArray(args[0] as Int))
        }

        // TODO: this is really bad...
        redirect("Lorg/lwjgl/opengl/GL15;glBufferData(IJI)V") { _: Any?, _: String?, _: Class<*>?, args: Array<Any?> ->
            currentBufferSize.set(args[1] as Long?)
            null
        }
        redirect("Lorg/lwjgl/opengl/GL15;glMapBuffer(II)" + "Ljava/nio/ByteBuffer;") { _: Any?, _: String?, _: Class<*>?, _: Array<Any?>? ->
            ByteBuffer.wrap(ByteArray(currentBufferSize.get().toInt()))
        }
        redirect("Lorg/lwjgl/system/MemoryUtil;" + "memAddress(Ljava/nio/ByteBuffer;)J", Redirection.of(1L))
        redirect(STBIImageRedirection.DESC, STBIImageRedirection.INSTANCE)
        redirect(MemASCIIRedirection.DESC, MemASCIIRedirection.INSTANCE)

        // act as if we compiled a shader program (blaze3d program)
        redirect("Lorg/lwjgl/opengl/GL20;glGetShaderi(II)I", Redirection.of(1))
        redirect("Lorg/lwjgl/opengl/GL20;glCreateProgram()I", Redirection.of(1))
        redirect("Lorg/lwjgl/opengl/GL20;glGetProgrami(II)I", Redirection.of(1))
        redirect("Lorg/lwjgl/openal/ALC10;alcOpenDevice(" + "Ljava/lang/CharSequence;)J", Redirection.of(1L))
        redirect("Lorg/lwjgl/system/MemoryUtil;memSlice(" + "Ljava/nio/ByteBuffer;II)Ljava/nio/ByteBuffer;", DefaultRedirections.NULL)
    }
    
}