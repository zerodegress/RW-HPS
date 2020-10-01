package com.github.dr.rwserver.util.file;

import com.github.dr.rwserver.struct.ObjectMap;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.log.Log;

import static com.github.dr.rwserver.util.Convert.castSeq;

/**
 * @author Dr
 */
public class LoadConfig {

    private ObjectMap<String, String> data = new ObjectMap<>();
    private FileUtil fileUtil;

    public LoadConfig(String file,String name) {
        fileUtil = FileUtil.File(file).toPath(name);
        Seq<String> line = castSeq(fileUtil.readFileData(true),String.class);

        line.each(e -> {
            String[] temp = e.split("=");
            if (temp.length > 1) {
                data.put(temp[0],temp[1]);
            }
        });
    }

	private String load(String input, Object def) {
        String result = data.get(input);
		
		if (result != null) {
            return result;
        }

		Log.warn("NO KEY- Please check the file",input);
		data.put(input,def.toString());
		return def.toString();
	}

    public String readString(String input) {
        return readString(input,null);
    }

    public String readString(String input,Object def) {
        String str = load(input,def);
        return str;
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
}