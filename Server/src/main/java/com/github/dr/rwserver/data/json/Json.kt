package com.github.dr.rwserver.data.json

import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.serializer.SerializerFeature
import com.github.dr.rwserver.struct.ObjectMap
import com.github.dr.rwserver.struct.Seq
import com.github.dr.rwserver.util.IsUtil

//Json
//写的越久，BUG越多，伤痕越疼，脾气越差/-活得越久 故事越多 伤痕越疼，脾气越差
/**
 * @author Dr
 */
class Json {
    private val jsonObject: JSONObject

    constructor(json: String) {
        jsonObject = JSONObject.parseObject(json)
    }

    constructor(jsonObject: JSONObject) {
        this.jsonObject = jsonObject
    }

    fun getData(str: String): String {
        return jsonObject.getString(str)
    }

    fun getDataNull(str: String): String? {
        return jsonObject.getString(str)
    }

    fun getInnerMap(): Map<String, Any> {
        return jsonObject.innerMap
    }

    fun getArrayData(str: String): Json? {
        val rArray = jsonObject.getJSONArray(str)
        for (o in rArray) {
            val r = o as JSONObject
            if (IsUtil.notIsBlank(r)) {
                return Json(r)
            }
        }
        return null
    }

    fun getArraySeqData(): Seq<Json> {
        val result = Seq<Json>()
        val rArray = jsonObject.getJSONArray("result")
        for (o in rArray) {
            val r = o as JSONObject
            if (IsUtil.notIsBlank(r)) {
                result.add(Json(r))
            }
        }
        return result
    }

    companion object {
        @JvmStatic
        fun toJson(map: ObjectMap<String, String>): String {
            return JSONObject.toJSONString(map, SerializerFeature.PrettyFormat)
        }
    }
}