package com.github.dr.rwserver.util

object ReflexUtil {
    @JvmStatic
    @Throws(Exception::class)
    fun modifyVariablesFinalStatic(type: Class<*>, name: String?, data: Any?) {
        // 反射获取字段, name成员变量
        val nameField = type.getDeclaredField(name)
        // 由于name成员变量是private, 所以需要进行访问权限设定
        nameField.isAccessible = true
        // 使用反射进行赋值
        nameField[type.getDeclaredConstructor().newInstance()] = data
    }
}