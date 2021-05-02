package com.github.dr.rwserver.net.web.api;

import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.data.json.Json;
import com.github.dr.rwserver.func.StrCons;
import com.github.dr.rwserver.net.web.realization.i.Central;
import com.github.dr.rwserver.struct.ObjectMap;
import com.github.dr.rwserver.util.encryption.Base64;

import java.util.Map;

/**
 * @author Dr
 */
@Central(url = "/api")
public class WebApi {
    @Central(url = "/runServerCommand")
    public String runServerCommand(String message, Map<Object, Object> map) {
        Json json = new Json(message);
        final StringBuilder str = new StringBuilder(8);
        Data.SERVERCOMMAND.handleMessage(json.getData("Command"),(StrCons) (e) -> str.append(e).append("\n"));
        ObjectMap<String,String> runServerCommand = new ObjectMap<>(4);
        runServerCommand.put("State","0");
        runServerCommand.put("Result",Base64.encode(str.toString()));
        return Base64.encode(Json.toJson(runServerCommand));
    }

    @Central(url = "/runPid")
    public String runPid(String message, Map<Object, Object> map) {
        ObjectMap<String,String> runPid = new ObjectMap<>(4);
        runPid.put("State","0");
        runPid.put("result",String.valueOf(Data.core.getPid()));
        return Base64.encode(Json.toJson(runPid));
    }
}