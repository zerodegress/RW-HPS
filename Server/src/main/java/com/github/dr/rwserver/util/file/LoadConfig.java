package com.github.dr.rwserver.util.file;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.dr.rwserver.struct.OrderedMap;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.log.Log;

import java.util.HashMap;
import java.util.Map;

import static com.github.dr.rwserver.data.global.Data.LINE_SEPARATOR;
import static com.github.dr.rwserver.util.Convert.castSeq;
import static com.github.dr.rwserver.util.IsUtil.isBlank;
import static com.github.dr.rwserver.util.IsUtil.notIsBlank;

/**
 * @author Dr
 */
public final class LoadConfig {

    private OrderedMap<String, String> data = new OrderedMap<>();
    private FileUtil fileUtil;
    private final boolean is;

    public LoadConfig(String file,boolean isFile) {
        fileUtil = isFile ? FileUtil.File(file) : new FileUtil(file);
        reLoadConfig();
        is = false;
    }

    public LoadConfig(String file,String name,boolean obj) {
        fileUtil = FileUtil.File(file).toPath(name);
        reLoadConfig2();
        is = true;
    }

    public LoadConfig(String file,String name) {
        fileUtil = FileUtil.File(file).toPath(name);
        reLoadConfig();
        is = false;
    }

    public void reLoadConfig() {
        JSONObject  jsonObject = JSONObject.parseObject(fileUtil.readFileData(false).toString(), Feature.OrderedField);
        //json对象转Map
        Map<String,Object> map = jsonObject.getInnerMap();
        map.forEach((k,v) -> data.put(k,v.toString()));
    }

    public void reLoadConfig2() {
        Seq<String> line = castSeq(fileUtil.readFileData(true),String.class);
        final char breakString = '#';
        final char breakString1 = ' ';

        line.each(e -> {
            try {
                String[] temp = e.split("=");
                if (e.charAt(0) != breakString && e.charAt(0) != breakString1) {
                    if (temp.length > 1) {
                        data.put(temp[0], temp[1]);
                    } else if (temp.length == 1) {
                        data.put(temp[0], "NOTAB-NULL");
                    }
                } else {
                    //data.put(temp[0], "NOTAB-#");
                }
            } catch (Exception ignored) {
            }
        });
        save();
    }

	private String load(String input, Object def) {
        String result = data.get(input);

		if (!"NOTAB-NULL".equals(result) && result != null) {
            return result;
        }

		if (!"NOTAB-NULL".equals(result) && notIsBlank(def)) {
            Log.warn("NO KEY- Please check the file",input);
        }
		data.put(input,def.toString());
        return def.toString();
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
        if (is) {
            final StringBuffer save = new StringBuffer();

            data.each((k,v) -> {
                if (v.equals("NOTAB-#")) {
                    save.append(k).append(LINE_SEPARATOR);
                } else {
                    save.append(k).append("=").append(v).append(LINE_SEPARATOR);
                }
            });
            return;
        }
        final Map<String,String> map = new HashMap<>();
        data.each((k,v) -> {
            map.put(k,v);
        });
        fileUtil.writeFile(JSONObject.toJSONString(map, SerializerFeature.PrettyFormat),false);
        Log.clog("SAVE CONFIG OK");
    }
}