package com.github.dr.rwserver.data.json

import com.github.dr.rwserver.struct.ObjectMap
import com.github.dr.rwserver.struct.Seq
import com.github.dr.rwserver.util.IsUtil
import com.github.dr.rwserver.util.serialization.JSONSerializer
import com.github.dr.rwserver.util.serialization.JsonFormatTool
import java.util.ArrayList
import java.util.LinkedHashMap

//Json
//写的越久，BUG越多，伤痕越疼，脾气越差/-活得越久 故事越多 伤痕越疼，脾气越差
/**
 * @author Dr
 */
class Json {
    private val jsonObject: Any

    constructor(json: String) {
        jsonObject = JSONSerializer.deserialize(json)
    }

    constructor(json: LinkedHashMap<*, *>) {
        jsonObject = json
    }

    fun getData(str: String): String {
        return (jsonObject as LinkedHashMap<*, *>)[str] as String
    }

    fun getDataNull(str: String): String? {
        return (jsonObject as LinkedHashMap<*, *>)[str] as String?
    }

    fun getInnerMap(): Map<String, Any> {
        return jsonObject as LinkedHashMap<String, Any>
    }

    fun getArrayData(str: String): Json? {
        val rArray = (jsonObject as LinkedHashMap<*, *>)[str] as ArrayList<*>
        for (o in rArray) {
            val r = o as LinkedHashMap<*, *>
            if (IsUtil.notIsBlank(r)) {
                return Json(r)
            }
        }
        return null
    }

    fun getArraySeqData(): Seq<Json> {
        val result = Seq<Json>()
        val rArray = (jsonObject as LinkedHashMap<*, *>)["result"] as ArrayList<*>
        for (o in rArray) {
            val r = o as LinkedHashMap<*, *>
            if (IsUtil.notIsBlank(r)) {
                result.add(Json(r))
            }
        }
        return result
    }

    companion object {
        /**
         * Map转Json 已格式化
         * @param map Map<String, Any>
         * @return Json
         */
        @JvmStatic
        fun toJson(map: Map<String, Any>): String {
            return JsonFormatTool().formatJson(JSONSerializer.serialize(map))
        }

        /**
         * Map转Json 已格式化
         * @param map Map<String, Any>
         * @return Json
         */
        @JvmStatic
        fun toJson(map: ObjectMap<String, Any>): String {
            return JsonFormatTool().formatJson(JSONSerializer.serialize(map))
        }
    }
}