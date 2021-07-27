package com.github.dr.rwserver.util.log

object Error {
    @JvmStatic
    fun error(type: String): String {
        return ErrorCode.valueOf(type).error
    }

    @JvmStatic
    fun code(type: String): Int {
        return ErrorCode.valueOf(type).code
    }
}