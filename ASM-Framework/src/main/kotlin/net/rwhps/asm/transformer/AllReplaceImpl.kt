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

package net.rwhps.asm.transformer

import net.rwhps.asm.data.MethodTypeInfoValue
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.TypeInsnNode

/**
 *
 *
 * @date 2023/10/22 12:33
 * @author Dr (dr@der.kim)
 */
class AllReplaceImpl {
    internal class AllMethodsTransformer: AbstractReplace() {
        override fun redirectCast(cast: TypeInsnNode, methodTypeInfoValue: MethodTypeInfoValue?): InsnList {
            return InsnList()
        }

        override fun injectRedirection(cn: ClassNode, mn: MethodNode, il: InsnList, methodTypeInfoValue: MethodTypeInfoValue?): Type {
            if (Type.getReturnType(mn.desc).sort == Type.VOID) {
                return Type.VOID_TYPE
            }
            return super.injectRedirection(cn, mn, il, methodTypeInfoValue)
        }
    }

    internal class PartialMethodTransformer: AbstractReplace()
}