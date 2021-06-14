package com.github.dr.rwserver.util.file;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.dr.rwserver.struct.OrderedMap;
import com.github.dr.rwserver.util.log.Log;

import java.util.HashMap;
import java.util.Map;

import static com.github.dr.rwserver.util.IsUtil.isBlank;

/**
 * @author Dr
 */
public final class LoadConfig {

    private final OrderedMap<String, String> data = new OrderedMap<>();
    private final FileUtil fileUtil;

    public LoadConfig(String file,boolean isFile) {
        fileUtil = isFile ? FileUtil.File(file) : new FileUtil(file);
        reLoadConfig();
    }

    public LoadConfig(String file,String name) {
        fileUtil = FileUtil.File(file).toPath(name);
        reLoadConfig();
    }

    public void reLoadConfig() {
        if(!fileUtil.exists()) {
            return;
        }
        JSONObject  jsonObject = JSONObject.parseObject(fileUtil.readFileData(false).toString(), Feature.OrderedField);
        //json对象转Map
        Map<String,Object> map = jsonObject.getInnerMap();
        map.forEach((k,v) -> data.put(k,v.toString()));
    }

	private String load(String input, Object def) {
        String result = data.get(input);
        if (result == null) {
            Log.clog("NO KEY- Please check the file",input);
            data.put(input,def.toString());
            return def.toString();
        }
        return result;
	}

    public String readString(String input) {
        return readString(input,null);
    }

    public String readString(String input,Object def) {
        String str = load(input,def);
        return isBlank(str) ? "" : str;
    }

    public int readInt(String input,Object def) {
        String str = load(input,def);
        return Integer.parseInt(str);
    }

    public boolean readBoolean(String input,Object def) {
        String str = load(input,def);
        return Boolean.parseBoolean(str);
    }

    public float readFloat(String input,Object def) {
        String str = load(input,def);
        return Float.parseFloat(str);
    }

    public long readLong(String input,Object def) {
        String str = load(input,def);
        return Long.parseLong(str);
    }

    public void setObject(String input,Object key) {
        data.put(input,key.toString());
    }

    public void save() {
        final Map<String,String> map = new HashMap<>();
        data.each(map::put);
        fileUtil.writeFile(JSONObject.toJSONString(map, SerializerFeature.PrettyFormat),false);
        Log.clog("SAVE CONFIG OK");
    }
}