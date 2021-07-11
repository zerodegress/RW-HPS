package com.github.dr.rwserver.util;

import java.lang.reflect.Field;

public class ReflexUtil {

    public static final void modifyVariablesFianlStatic(Class<?> type,String name,Object data) throws Exception {
        // 反射获取字段, name成员变量
        Field nameField = type.getDeclaredField(name);
        // 由于name成员变量是private, 所以需要进行访问权限设定
        nameField.setAccessible(true);
        // 使用反射进行赋值
        nameField.set(type.getDeclaredConstructor().newInstance(), data);
    }
}
