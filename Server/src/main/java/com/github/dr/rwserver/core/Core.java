package com.github.dr.rwserver.core;

import com.github.dr.rwserver.core.ex.Threads;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.util.encryption.Base64;
import com.github.dr.rwserver.util.file.FileUtil;

import static com.github.dr.rwserver.net.HttpRequest.doPost;

/**
 * @author Dr
 */
public class Core {
    public static void exit() {
        Data.core.save();
        Threads.close();
        System.exit(0);
    }

    public static void upLog() {
        FileUtil file = FileUtil.File(Data.Plugin_Log_Path).toPath("Log.log");
        if (!file.exists()) {
            return;
        }
        StringBuffer data = new StringBuffer(64);
        data.append(Data.SERVER_CORE_VERSION)
                .append("\n")
                .append(new Base64().encode((String) file.readFileData(false)));
        doPost("https://api.mindustry.top:60443/api/post/log",data.toString());
        file.getFile().delete();
    }
}
