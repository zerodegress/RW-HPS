/*
 *
 *  * Copyright 2020-2023 RW-HPS Team and contributors.
 *  *
 *  * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  *
 *  * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 *
 */

package net.rwhps.asm.agent

import net.rwhps.asm.api.Transformer
import net.rwhps.asm.data.RedirectionsDataManager
import net.rwhps.asm.func.Find
import net.rwhps.asm.transformer.AllReplaceImpl
import net.rwhps.asm.util.transformer.AsmUtil
import org.objectweb.asm.ClassWriter
import java.lang.instrument.ClassFileTransformer
import java.lang.instrument.IllegalClassFormatException
import java.lang.instrument.Instrumentation
import java.security.ProtectionDomain

/**
 * A JavaAgent calling the [AllMethodsTransformer] and [PartialMethodTransformer].
 */
class AsmAgent: ClassFileTransformer, AsmCore() {

    private val transformer: Transformer = AllReplaceImpl.AllMethodsTransformer()
    private val partialMethodTransformer: Transformer = AllReplaceImpl.PartialMethodTransformer()

    init {
        setAgent(this)
    }

    /**
     * Transforms the given class file and returns a new replacement class file.
     * This method is invoked when the {@link Module Module} bearing {@link
     * ClassFileTransformer#transform(Module,ClassLoader,String,Class,ProtectionDomain,byte[])
     * transform} is not overridden.
     *
     * @param loader                the defining loader of the class to be transformed,
     *                              may be {@code null} if the bootstrap loader
     * @param className             the name of the class in the internal form of fully
     *                              qualified class and interface names as defined in
     *                              <i>The Java Virtual Machine Specification</i>.
     *                              For example, <code>"java/util/List"</code>.
     * @param classBeingRedefined   if this is triggered by a redefine or retransform,
     *                              the class being redefined or retransformed;
     *                              if this is a class load, {@code null}
     * @param protectionDomain      the protection domain of the class being defined or redefined
     * @param classfileBuffer       the input byte buffer in class file format - must not be modified
     *
     * @throws IllegalClassFormatException
     *         if the input does not represent a well-formed class file
     * @return a well-formed class file buffer (the result of the transform),
     *         or {@code null} if no transform is performed
     */
    @Throws(IllegalClassFormatException::class)
    override fun transform(
        loader: ClassLoader?,
        className: String,
        classBeingRedefined: Class<*>?,
        protectionDomain: ProtectionDomain?,
        classfileBuffer: ByteArray
    ): ByteArray {
        if (RedirectionsDataManager.allClassPathCache.contains(className) ||
            RedirectionsDataManager.allReplaceFindPacketName.forFind(className)
        ) {
            val node = AsmUtil.read(classfileBuffer)
            transformer.transform(node)
            if (RedirectionsDataManager.partialClassPathCache.contains(className)) {
                partialMethodTransformer.transform(node, RedirectionsDataManager.partialReplaceMethodName[className])
            }
            return AsmUtil.write(loader, node, ClassWriter.COMPUTE_FRAMES)
        }

        if (RedirectionsDataManager.partialClassPathCache.contains(className)) {
            val node = AsmUtil.read(classfileBuffer)
            partialMethodTransformer.transform(node, RedirectionsDataManager.partialReplaceMethodName[className])
            return AsmUtil.write(loader, node, ClassWriter.COMPUTE_FRAMES)
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
        @JvmStatic
        fun agentmain(instrumentation: Instrumentation) {
            instrumentation.addTransformer(AsmAgent())
        }
    }
}