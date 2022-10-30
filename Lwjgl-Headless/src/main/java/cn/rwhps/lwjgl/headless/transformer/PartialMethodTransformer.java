/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.lwjgl.headless.transformer;

import cn.rwhps.lwjgl.headless.api.RedirectionApi;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * A given {@link ClassNode} will be transformed in the following ways:
 * <p>-if it's a module a {@code requires headlessmc.lwjgl} will be added.
 * <p>-the no-args constructor will be made public or created if necessary
 * <p>-Every method body will have its code removed and will call
 * {@link RedirectionApi#invoke(Object, String, Class, Object...)}.
 * <p>-the constructor will keep its body, a {@link RedirectionApi#invoke(Object,
 * String, Class, Object...)} will be added before all RETURN instructions in
 * the constructor. Class casts in constructors will be redirected.
 * <p>-if the class is abstract and not an interface the abstract modifier
 * will be removed.
 * <p>-All abstract and native methods will be turned into normal methods, with
 * their body transformed as described above.
 */
public class PartialMethodTransformer extends AllMethodsTransformer {
    @Override
    public void transform(ClassNode cn, String[] parameters) {
        try {
            transformModule(cn);
        } catch (NoSuchFieldError ignored) {
        }

        if ((cn.access & ACC_MODULE) == 0) {
            patchClass(cn, (cn.access & ACC_INTERFACE) != 0,parameters);
            // make all non-static fields non-final
            for (FieldNode fn : cn.fields) {
                if ((fn.access & ACC_STATIC) == 0) {
                    fn.access &= ~ACC_FINAL;
                }
            }
        }
    }

    protected void patchClass(ClassNode cn, boolean isInterface, String[] parameters) {
        boolean shouldAddNoArgsCtr = true;
        // TODO: while we can implement all abstract methods which are directly
        //  present in the class, methods inherited from an interface or another
        //  abstract class may pose a problem.
        //  We could at least warn every time a formerly abstract class is being
        //  instantiated?
        if (!isInterface) {
            cn.access &= ~ACC_ABSTRACT;
        }

        for (MethodNode mn : cn.methods) {
            if (mn.name.equals("<init>") && mn.desc.equals("()V")) {
                // TODO: check this
                mn.access |= ACC_PUBLIC;
                mn.access &= ~(ACC_PROTECTED | ACC_PRIVATE);
                shouldAddNoArgsCtr = false;
            }

            if (!isInterface
                || (mn.access & ACC_STATIC) != 0
                || (mn.access & ACC_ABSTRACT) == 0) {
                // Filter out the specified method
                if (mn.name.equals(parameters[0]) && mn.desc.equals(parameters[1])){
                    redirect(mn, cn);
                }
            }
        }

        // TODO: super class containing lwjgl is no guarantee for it to
        //  have a default constructor!
        if (shouldAddNoArgsCtr
            && !isInterface
            && (cn.superName == null
            || cn.superName.toLowerCase().contains("lwjgl")
            || cn.superName.equals(Type.getInternalName(Object.class)))) {
            // Add NoArgs Constructor for the ObjectRedirection
            MethodNode mn = new MethodNode(ACC_PUBLIC, "<init>", "()V",
                                           null, new String[0]);
            InsnList il = new InsnList();
            mn.instructions = il;
            il.add(new VarInsnNode(ALOAD, 0));
            il.add(new MethodInsnNode(INVOKESPECIAL, cn.superName, "<init>",
                                      "()V", false));
            injectRedirection(cn, mn, il);
            il.add(new InsnNode(RETURN));
            cn.methods.add(mn);
            mn.visitMaxs(0, 0);
        }
    }
}
