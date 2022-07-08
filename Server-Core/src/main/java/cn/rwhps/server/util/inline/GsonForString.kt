/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

@file:JvmName("StringUtils")
@file:JvmMultifileClass

package cn.rwhps.server.util.inline

import com.google.gson.Gson
import com.google.gson.GsonBuilder

val gsonToString: Gson = GsonBuilder().setPrettyPrinting().create()
val stringToGson: Gson = Gson()

fun Any.toPrettyPrintingJson() : String {
    return gsonToString.toJson(this)
}

inline fun <reified T> Class<T>.toGson(classData: String) : T {
    return stringToGson.fromJson(classData.ifBlank { "{}" }, T::class.java)
}
