package com.github.dr.rwserver.util

import com.github.dr.rwserver.struct.Seq

/**
 * @author Dr
 */
object Convert {
    @JvmStatic
    fun <T> castList(obj: Any?, clazz: Class<T>): List<T>? {
        val result: MutableList<T> = ArrayList()
        if (obj is List<*>) {
            for (o in obj) {
                result.add(clazz.cast(o))
            }
            return result
        }
        return null
    }

    @JvmStatic
    fun <T> castSeq(obj: Any?, clazz: Class<T>): Seq<T>? {
        val result = Seq<T>()
        if (obj is Seq<*>) {
            for (o in obj) {
                result.add(clazz.cast(o))
            }
            return result
        }
        return null
    }
}