package com.github.dr.rwserver.util.file;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.dr.rwserver.data.json.Json;
import com.github.dr.rwserver.struct.OrderedMap;
import com.github.dr.rwserver.util.log.Log;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.github.dr.rwserver.util.IsUtil.isBlank;

/**
 * @author Dr
 */
public final class LoadConfig {

    private final OrderedMap<String, Object> data = new OrderedMap<>();
    private final FileUtil fileUtil;

    public LoadConfig(String file,boolean isFile) {
        fileUtil = isFile ? FileUtil.getFolder(file) : new FileUtil(file);
        reLoadConfig();
    }

    public LoadConfig(String file,String name) {
        fileUtil = FileUtil.getFolder(file).toFile(name);
        reLoadConfig();
    }

    public void reLoadConfig() {
        if (fileUtil.notExists() || fileUtil.readFileStringData().isEmpty()) {
            Log.error("NO Config.Json Use default configuration");
            return;
        }
        Json json = new Json(fileUtil.readFileStringData());
        //json对象转Map
        json.getInnerMap().forEach((k,v) -> data.put(k,v.toString()));
    }

    private String load(String input, Object def) {
        Object result = data.get(input);
        if (result == null) {
            Log.clog("NO KEY- Please check the file",input);
            data.put(input,def);
            return def.toString();
        }
        return result.toString();
    }

    public String readString(String input) {
        return readString(input,"");
    }

    public String readString(String input,@NotNull Object def) {
        String str = load(input,def);
        return isBlank(str) ? "" : str;
    }

    public int readInt(String input, int def) {
        String str = load(input,def);
        return Integer.parseInt(str);
    }

    public boolean readBoolean(String input,boolean def) {
        String str = load(input,def);
        return Boolean.parseBoolean(str);
    }

    public float readFloat(String input,float def) {
        String str = load(input,def);
        return Float.parseFloat(str);
    }

    public long readLong(String input,long def) {
        String str = load(input,def);
        return Long.parseLong(str);
    }

    public void setObject(String input,@NotNull Object key) {
        data.put(input,key.toString());
    }

    public void save() {
        final Map<String,Object> map = new HashMap<>();
        data.each(map::put);
        fileUtil.writeFile(JSONObject.toJSONString(map, SerializerFeature.PrettyFormat),false);
        Log.clog("SAVE CONFIG OK");
    }
}