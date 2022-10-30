/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.lwjgl.headless.agent

import cn.rwhps.lwjgl.headless.api.Transformer
import cn.rwhps.lwjgl.headless.transformer.AllMethodsTransformer
import cn.rwhps.lwjgl.headless.transformer.AsmUtil
import cn.rwhps.lwjgl.headless.transformer.PartialMethodTransformer
import org.objectweb.asm.ClassWriter
import java.lang.instrument.ClassFileTransformer
import java.lang.instrument.IllegalClassFormatException
import java.lang.instrument.Instrumentation
import java.security.ProtectionDomain

/**
 * A JavaAgent calling the [AllMethodsTransformer].
 */
class LwjglAgent : ClassFileTransformer {

    private val transformer: Transformer = AllMethodsTransformer()
    private val partialMethodTransformer: Transformer = PartialMethodTransformer()

    @Throws(IllegalClassFormatException::class)
    override fun transform(loader: ClassLoader, className: String, classBeingRedefined: Class<*>?, protectionDomain: ProtectionDomain, classfileBuffer: ByteArray): ByteArray {
        // 覆写 lwjgl 并跳过 RW-HPS 包
        if (className.contains("lwjgl") && !className.contains("rwhps")) {
            val node = AsmUtil.read(classfileBuffer)
            transformer.transform(node)
            // TODO: make writer.getClassLoader() return the given loader?
            return AsmUtil.write(node, ClassWriter.COMPUTE_FRAMES)
        }
        if (partialMethod.containsKey(className)) {
            val node = AsmUtil.read(classfileBuffer)
            partialMethodTransformer.transform(node, partialMethod[className])
            return AsmUtil.write(node, ClassWriter.COMPUTE_FRAMES)
        }
        // 覆写 让类全部空实现
        if (className.contains("org/newdawn/slick/util/DefaultLogSystem") || className == "com/LibRocket" || className == "com/corrodinggames/librocket/scripts/ScriptEngine") {
            val node = AsmUtil.read(classfileBuffer)
            transformer.transform(node)
            return AsmUtil.write(node, ClassWriter.COMPUTE_FRAMES)
        }
        return classfileBuffer
    }

    companion object {
        val partialMethod = HashMap<String, Array<String>>()

        fun agentmain(args: String?, instrumentation: Instrumentation) {
            instrumentation.addTransformer(LwjglAgent())
        }
    }
}