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

package net.rwhps.asm

import net.rwhps.asm.api.Transformer
import net.rwhps.asm.data.MethodTypeInfoValue
import net.rwhps.asm.transformer.AllReplaceImpl
import net.rwhps.asm.util.transformer.AsmUtil
import org.objectweb.asm.ClassWriter
import java.io.FileOutputStream

/**
 *
 *
 * @date 2023/10/28 17:23
 * @author Dr (dr@der.kim)
 */
class Test {
    fun a(loader: ClassLoader, classfileBuffer: ByteArray, fileOutputStream: FileOutputStream) {
        val transformer: Transformer = AllReplaceImpl.AllMethodsTransformer()

        val node = AsmUtil.read(classfileBuffer)
        transformer.transform(node)

        fileOutputStream.write(AsmUtil.write(loader, node, ClassWriter.COMPUTE_FRAMES))
        fileOutputStream.flush()
    }

    fun b(loader: ClassLoader, classfileBuffer: ByteArray, fileOutputStream: FileOutputStream) {
        val transformer: Transformer = AllReplaceImpl.AllMethodsTransformer()
        val partialMethodTransformer: Transformer = AllReplaceImpl.PartialMethodTransformer()

        val node = AsmUtil.read(classfileBuffer)
        transformer.transform(node)
        partialMethodTransformer.transform(node, ArrayList(listOf(MethodTypeInfoValue("", "f","()Ljava/lang/String;", Any::class.java))))

        fileOutputStream.write(AsmUtil.write(loader, node, ClassWriter.COMPUTE_FRAMES))
        fileOutputStream.flush()
    }
}