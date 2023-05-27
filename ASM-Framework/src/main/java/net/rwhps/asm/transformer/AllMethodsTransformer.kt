/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */
package net.rwhps.asm.transformer

import net.rwhps.asm.api.Redirection
import net.rwhps.asm.api.RedirectionApi
import net.rwhps.asm.api.Transformer
import net.rwhps.asm.transformer.InstructionUtil.box
import net.rwhps.asm.transformer.InstructionUtil.loadParam
import net.rwhps.asm.transformer.InstructionUtil.loadType
import net.rwhps.asm.transformer.InstructionUtil.makeReturn
import net.rwhps.asm.transformer.InstructionUtil.unbox
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import java.lang.reflect.Modifier
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
open class AllMethodsTransformer : Transformer {
    override fun transform(classNode: ClassNode, parameters: ArrayList<Array<String>>?) {
        try {
            transformModule(classNode)
        } catch (ignored: NoSuchFieldError) {
            // If we run this Transformer via the LaunchWrapper we could be on
            // an older ASM version which doesnt have the module field yet.
        }
        if (classNode.access and Opcodes.ACC_MODULE == 0) {
            patchClass(classNode, classNode.access and Opcodes.ACC_INTERFACE != 0)
            // make all non-static fields non-final
            for (fn in classNode.fields) {
                if (fn.access and Opcodes.ACC_STATIC == 0) {
                    fn.access = fn.access and Opcodes.ACC_FINAL.inv()
                }
            }
        }
    }

    protected fun transformModule(cn: ClassNode) {
        if (cn.module != null) {
            cn.module.visitRequire("rwhps.asm", Opcodes.ACC_MANDATED, null)
            cn.module.access = cn.module.access or Opcodes.ACC_OPEN
        }
    }

    protected fun injectRedirection(cn: ClassNode, mn: MethodNode, il: InsnList): Type {
        val isStatic = Modifier.isStatic(mn.access)
        if (isStatic) {
            il.add(LdcInsnNode(Type.getType("L" + cn.name + ";")))
        } else {
            il.add(VarInsnNode(Opcodes.ALOAD, 0))
        }
        il.add(LdcInsnNode("L" + cn.name + ";" + mn.name + mn.desc))
        val returnType = Type.getReturnType(mn.desc)
        il.add(loadType(returnType))
        loadArgArray(mn.desc, il, isStatic)
        il.add(MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(RedirectionApi::class.java), Redirection.METHOD_NAME, Redirection.METHOD_DESC))
        il.add(unbox(returnType))
        return returnType
    }

    protected fun patchClass(cn: ClassNode, isInterface: Boolean) {
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
                redirect(mn, cn)
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

    protected fun redirect(mn: MethodNode, cn: ClassNode) {
        if ("<init>" == mn.name && mn.desc.endsWith(")V")) {
            // There's not really a way to determine when this() or super()
            // is called in a constructor, because of that we'll just inject
            // the RedirectionManager at every RETURN instruction.
            // This hasn't become a problem, yet...
            var insnNode = mn.instructions.first
            while (insnNode != null) {
                if (insnNode is TypeInsnNode && insnNode.getOpcode() == Opcodes.CHECKCAST) {
                    mn.instructions.insertBefore(insnNode, redirectCast(insnNode))
                } else if (insnNode.opcode == Opcodes.RETURN) {
                    val il = InsnList()
                    injectRedirection(cn, mn, il)
                    mn.instructions.insertBefore(insnNode, il)
                }
                insnNode = insnNode.next
            }
        } else {
            val il = InsnList()
            il.add(makeReturn(injectRedirection(cn, mn, il)))
            mn.instructions = il
        }
        mn.tryCatchBlocks = ArrayList()
        mn.localVariables = ArrayList()
        mn.parameters = ArrayList()
        mn.access = mn.access and Opcodes.ACC_NATIVE.inv() and Opcodes.ACC_ABSTRACT.inv()
    }

    protected fun redirectCast(cast: TypeInsnNode): InsnList {
        val il = InsnList()
        il.add(LdcInsnNode(Redirection.CAST_PREFIX + cast.desc))
        // TODO: does this really cover all cases?
        if (cast.desc.startsWith("[")) {
            il.add(LdcInsnNode(Type.getType(cast.desc)))
        } else {
            il.add(LdcInsnNode(Type.getType("L" + cast.desc + ";")))
        }
        loadArgArray("()V", il, false) // create empty array
        // 在这里修改全部的方法 并代理
        il.add(MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(RedirectionApi::class.java), Redirection.METHOD_NAME, Redirection.METHOD_DESC))
        return il
    }

    private fun loadArgArray(desc: String?, il: InsnList, isStatic: Boolean) {
        val args = Type.getArgumentTypes(desc)
        il.add(LdcInsnNode(args.size))
        il.add(TypeInsnNode(Opcodes.ANEWARRAY, Type.getInternalName(Any::class.java)))
        var i = 0
        var v = if (isStatic) 0 else 1
        while (i < args.size) {
            il.add(InsnNode(Opcodes.DUP))
            val type = args[i]
            il.add(LdcInsnNode(i))
            il.add(loadParam(type, v))
            if (type.sort == Type.DOUBLE || type.sort == Type.LONG) {
                // double and long take up two registers, so we skip one
                v++
            }
            val boxing = box(type)
            if (boxing != null) {
                il.add(boxing)
            }
            il.add(InsnNode(Opcodes.AASTORE))
            i++
            v++
        }
    }
}
