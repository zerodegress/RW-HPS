/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */
package net.rwhps.asm.transformer

import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import java.util.*


/**
 * A given [ClassNode] will be transformed in the following ways:
 *
 * -if it's a module a `requires headlessmc.lwjgl` will be added.
 *
 * -the no-args constructor will be made public or created if necessary
 *
 * -Every method body will have its code removed and will call
 * [RedirectionApi.invoke].
 *
 * -the constructor will keep its body, a [RedirectionApi.invoke] will be added before all RETURN instructions in
 * the constructor. Class casts in constructors will be redirected.
 *
 * -if the class is abstract and not an interface the abstract modifier
 * will be removed.
 *
 * -All abstract and native methods will be turned into normal methods, with
 * their body transformed as described above.
 */
open class PartialMethodTransformer : AllMethodsTransformer() {
    override fun transform(cn: ClassNode, parameters: ArrayList<Array<String>>?) {
        try {
            transformModule(cn)
        } catch (ignored: NoSuchFieldError) {
            // Ignore
        }

        if (cn.access and Opcodes.ACC_MODULE == 0) {
            patchClass(cn, cn.access and Opcodes.ACC_INTERFACE != 0, parameters!!)
            // make all non-static fields non-final
            for (fn in cn.fields) {
                if (fn.access and Opcodes.ACC_STATIC == 0) {
                    fn.access = fn.access and Opcodes.ACC_FINAL.inv()
                }
            }
        }
    }
    private fun patchClass(cn: ClassNode, isInterface: Boolean, parameters: ArrayList<Array<String>>) {
        var shouldAddNoArgsCtr = true
        // TODO: while we can implement all abstract methods which are directly
        //  present in the class, methods inherited from an interface or another
        //  abstract class may pose a problem.
        //  We could at least warn every time a formerly abstract class is being
        //  instantiated?
        if (!isInterface) {
            cn.access = cn.access and Opcodes.ACC_ABSTRACT.inv()
        }
        for (mn in cn.methods) {
            if (mn.name == "<init>" && mn.desc == "()V") {
                // TODO: check this
                mn.access = mn.access or Opcodes.ACC_PUBLIC
                mn.access = mn.access and (Opcodes.ACC_PROTECTED or Opcodes.ACC_PRIVATE).inv()
                shouldAddNoArgsCtr = false
            }
            if (!isInterface || mn.access and Opcodes.ACC_STATIC != 0 || mn.access and Opcodes.ACC_ABSTRACT == 0) {
                // Filter out the specified method
                for (p in parameters) {
                    if (mn.name == p[0] && mn.desc == p[1]) {
                        redirect(mn, cn)
                    }
                }
            }
        }

        // TODO: super class containing lwjgl is no guarantee for it to
        //  have a default constructor!
        if (shouldAddNoArgsCtr &&
            !isInterface &&
            (cn.superName == null || cn.superName.lowercase(Locale.getDefault()).contains("lwjgl") || cn.superName == Type.getInternalName(Any::class.java))
        ) {
            // Add NoArgs Constructor for the ObjectRedirection
            val mn = MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, arrayOfNulls(0))
            val il = InsnList()
            mn.instructions = il
            il.add(VarInsnNode(Opcodes.ALOAD, 0))
            il.add(MethodInsnNode(Opcodes.INVOKESPECIAL, cn.superName, "<init>", "()V", false))
            injectRedirection(cn, mn, il)
            il.add(InsnNode(Opcodes.RETURN))
            cn.methods.add(mn)
            mn.visitMaxs(0, 0)
        }
    }
}
