package com.github.dr.rwserver.data.json;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.dr.rwserver.struct.ObjectMap;
import com.github.dr.rwserver.struct.Seq;

import java.util.Map;

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

    public Json(JSONObject jsonObject) {
        this.JsonObject = jsonObject;
    }

	public String getData(String str){
		return JsonObject.getString(str);
	}

	public Map<String, Object> getInnerMap() {
        return JsonObject.getInnerMap();
    }

    public Json getArrayData(String str){
        JSONArray rArray = JsonObject.getJSONArray(str);
        for (Object o : rArray) {
            JSONObject r = (JSONObject) o;
            if (notIsBlank(r)) {
                return new Json(r);
            }
        }
        return null;
    }

    public Seq<Json> getArraySeqData() {
        final Seq<Json> result = new Seq<>();
        JSONArray rArray = JsonObject.getJSONArray("result");
        for (Object o : rArray) {
            JSONObject r = (JSONObject) o;
            if (notIsBlank(r)) {
                result.add(new Json(r));
            }
        }
        return result;
    }

	public static String toJson(ObjectMap<String,String> map) {
        return JSONObject.toJSONString(map, SerializerFeature.PrettyFormat);
	}

}