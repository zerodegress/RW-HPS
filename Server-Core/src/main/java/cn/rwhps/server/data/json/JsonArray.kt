/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.data.json

import cn.rwhps.server.util.inline.getDataResult
import org.json.JSONArray

class JsonArray {
    private val jsonObject: JSONArray

    constructor(json: String) {
        jsonObject = JSONArray(json)
    }

    constructor(json: Array<Any>) {
        jsonObject = JSONArray(json)
    }

    constructor(json: JSONArray) {
        jsonObject = json
    }

    @JvmOverloads
    fun getString(index: Int, defaultValue: String = ""): String {
        return jsonObject.getDataResult(defaultValue) {
            it.getString(index)
        }!!
    }
    @JvmOverloads
    fun getInt(index: Int, defaultValue: Int? = null): Int? {
        return jsonObject.getDataResult(defaultValue) {
            it.getInt(index)
        }
    }
    @JvmOverloads
    fun getLong(index: Int, defaultValue: Long? = null): Long? {
        return jsonObject.getDataResult(defaultValue) {
            it.getLong(index)
        }
    }
    @JvmOverloads
    fun getBoolean(index: Int, defaultValue: Boolean? = null): Boolean? {
        return jsonObject.getDataResult(defaultValue) {
            it.getBoolean(index)
        }
    }
}
