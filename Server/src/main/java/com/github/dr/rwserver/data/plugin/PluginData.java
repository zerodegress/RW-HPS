package com.github.dr.rwserver.data.plugin;

import com.github.dr.rwserver.util.file.Settings;

@SuppressWarnings("unchecked")
public class PluginData {
    private final Settings settings = new Settings();

    public PluginData() {
    }

    public void load() {
        settings.load();
    }

    public <T> T getData(String name,T object) {
        if(object instanceof Boolean){
            return (T) object.getClass().cast(settings.getBoolean(name, (Boolean) object));
        }else if(object instanceof Integer){
            return (T) object.getClass().cast(settings.getInt(name,(Integer) object));
        }else if(object instanceof Long){
            return (T) object.getClass().cast(settings.getLong(name,(Long) object));
        }else if(object instanceof Float){
            return (T) object.getClass().cast(settings.getFloat(name,(Float) object));
        }else if(object instanceof String){
            return (T) settings.getString(name,(String) object);
        }
         return settings.getObjectClass(name,object.getClass(),object);
    }

    public void put(String name, Object object) {
        settings.put(name,object);
    }

    public void putObject(String name, Object object) {
        settings.putObject(name,object);
    }

    public void save() {
        settings.saveData();
    }
}
