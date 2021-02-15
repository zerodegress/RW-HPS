package com.github.dr.rwserver.data.json;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.dr.rwserver.struct.ObjectMap;

import static com.github.dr.rwserver.util.IsUtil.notIsBlank;
//Json
//写的越久，BUG越多，伤痕越疼，脾气越差/-活得越久 故事越多 伤痕越疼，脾气越差

/**
 * @author Dr
 */
public class Json {

    private final JSONObject JsonObject;

    public Json(String json) {
        this.JsonObject = JSONObject.parseObject(json);
    }

    public Json(JSONObject JsonObject) {
        this.JsonObject = JsonObject;
    }

	public String getData(String str){
		return JsonObject.getString(str);
	}

    public Json getArrayData(String str){
        JSONArray rArray = JsonObject.getJSONArray(str);
        for (int i = 0; i < rArray.size(); i++) {
            JSONObject r = (JSONObject)rArray.get(i);
            if (notIsBlank(r)) {
                return new Json(r);
            }
        }
        return null;
    }

	public static String toJson(ObjectMap map) {
		String json = JSONObject.toJSONString(map, SerializerFeature.PrettyFormat);
		return json;
	}

}