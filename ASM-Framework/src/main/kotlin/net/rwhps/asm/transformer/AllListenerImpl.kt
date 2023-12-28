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

import net.rwhps.asm.api.listener.RedirectionListener
import net.rwhps.asm.api.listener.RedirectionListenerApi
import net.rwhps.asm.data.MethodTypeInfoValue
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import java.lang.reflect.Modifier

/**
 *
 *
 * @date 2023/10/22 12:33
 * @author Dr (dr@der.kim)
 */
class AllListenerImpl {
    internal class ListenerPartialMethodsTransformer: AbstractReplace() {
        override fun redirectMain(mn: MethodNode, cn: ClassNode, methodTypeInfoValue: MethodTypeInfoValue?) {
            if (methodTypeInfoValue == null) {
                throw NullPointerException("redirectMain: MethodTypeInfoValue Null")
            }
            val il = InsnList()
            injectRedirection(cn, mn, il, methodTypeInfoValue)

            if (methodTypeInfoValue.listenerBefore) {
                mn.instructions.insertBefore(mn.instructions.first, il)
            } else {
                mn.instructions.insertBefore(mn.instructions.last, il)
            }
        }

        override fun injectRedirection(cn: ClassNode, mn: MethodNode, il: InsnList, methodTypeInfoValue: MethodTypeInfoValue?): Type {
            val isStatic = Modifier.isStatic(mn.access)
            if (isStatic) {
                il.add(LdcInsnNode(Type.getType("L" + cn.name + ";")))
            } else {
                il.add(VarInsnNode(Opcodes.ALOAD, 0))
            }
            il.add(LdcInsnNode("L" + cn.name + ";" + mn.name + mn.desc))
            val returnType = Type.VOID_TYPE
            //il.add(InstructionUtil.loadType(returnType))
            loadArgArray(mn.desc, il, isStatic)

            addMethodInsnNode(il, methodTypeInfoValue)
            //il.add(InstructionUtil.unbox(returnType))
            return returnType
        }

        override fun addMethodInsnNode(il: InsnList, methodTypeInfoValue: MethodTypeInfoValue?) {
            if (methodTypeInfoValue?.listenerClass == null) {
                il.add(MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(RedirectionListenerApi::class.java), RedirectionListener.METHOD_NAME, RedirectionListener.METHOD_DESC))
            } else {
                il.insertBefore(il.first, FieldInsnNode(
                    Opcodes.GETSTATIC,
                    Type.getInternalName(methodTypeInfoValue.listenerClass),
                    "INSTANCE",
                    "L"+Type.getInternalName(methodTypeInfoValue.listenerClass)+";"
                ))
                il.add(MethodInsnNode(
                    Opcodes.INVOKEVIRTUAL,
                    Type.getInternalName(methodTypeInfoValue.listenerClass),
                    RedirectionListener.METHOD_NAME,
                    RedirectionListener.METHOD_DESC
                ))
            }
        }
    }
}