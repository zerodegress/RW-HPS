/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.asm.agent

import net.rwhps.asm.api.Transformer
import net.rwhps.asm.func.Find
import net.rwhps.asm.transformer.AllMethodsTransformer
import net.rwhps.asm.transformer.AsmUtil
import net.rwhps.asm.transformer.PartialMethodTransformer
import org.objectweb.asm.ClassWriter
import java.io.FileOutputStream
import java.lang.instrument.ClassFileTransformer
import java.lang.instrument.IllegalClassFormatException
import java.lang.instrument.Instrumentation
import java.security.ProtectionDomain

/**
 * A JavaAgent calling the [AllMethodsTransformer].
 */
class AsmAgent : ClassFileTransformer, AsmCore() {

    private val transformer: Transformer = AllMethodsTransformer()
    private val partialMethodTransformer: Transformer = PartialMethodTransformer()

    init {
        setAgent(this)
    }

    @Throws(IllegalClassFormatException::class)
    override fun transform(loader: ClassLoader?, className: String, classBeingRedefined: Class<*>?, protectionDomain: ProtectionDomain?, classfileBuffer: ByteArray): ByteArray {
        // 这个是单纯的 Debug 用的
        if (className.contains("com/corrodinggames/rts/gameFramework/j/d000")) {
            val node = AsmUtil.read(classfileBuffer)
            transformer.transform(node)
            return AsmUtil.write(loader, node, ClassWriter.COMPUTE_FRAMES).also { a -> FileOutputStream("a.class").also { it.write(a); it.flush() }}
        }
        if (allIgnore.forFind(className)
            // 匹配 Class 并且替换全部方法
            || allMethod.contains(className)) {
            val node = AsmUtil.read(classfileBuffer)
            transformer.transform(node)
            return AsmUtil.write(loader, node, ClassWriter.COMPUTE_FRAMES)
        }
        // 匹配指定 Class 的指定方法
        if (partialMethod.containsKey(className)) {
            val node = AsmUtil.read(classfileBuffer)
            partialMethodTransformer.transform(node, partialMethod[className])
            return AsmUtil.write(loader, node, ClassWriter.COMPUTE_FRAMES)
                //.also { a -> FileOutputStream("${className.replace("/","")}.class").also { it.write(a); it.flush() }}
        }
        return classfileBuffer
    }

    private fun ArrayList<Find<String, Boolean>>.forFind(value: String): Boolean {
        var result = false
        this.forEach {
            result = it(value)
            if (result) {
                return result
            }
        }
        return result
    }

    companion object {
        fun agentmain(instrumentation: Instrumentation) {
            instrumentation.addTransformer(AsmAgent())
        }
    }
}