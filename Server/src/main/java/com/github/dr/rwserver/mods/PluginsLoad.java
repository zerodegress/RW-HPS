package com.github.dr.rwserver.mods;

import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.alone.JSONSerializer;
import com.github.dr.rwserver.util.file.FileUtil;
import com.github.dr.rwserver.util.file.ZipFi;
import com.github.dr.rwserver.util.log.Log;

import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashMap;
import java.util.List;

import static com.github.dr.rwserver.util.IsUtil.isBlank;
import static com.github.dr.rwserver.util.IsUtil.notIsBlank;

/**
 * @author Dr
 */
public class PluginsLoad {

    final Seq<File> jarFileList = new Seq<>();

    public PluginsLoad(FileUtil f) {
        List<File> list = f.getFileList();
        for (File file : list) {
            if (file.getName().endsWith("jar")) {
                jarFileList.add(file);
            }
        }
    }

    public Seq<PluginData> loadJar() {
        Seq<PluginData> data = new Seq<>();
        Seq<String> dataName = new Seq<>();
        Seq<PluginImportData> dataImport = new Seq<>();
        LinkedHashMap map = null;
        for (File file : jarFileList) {
            InputStreamReader imp = new ZipFi(file).getZipInputStream();
            if (isBlank(imp)) {
                Log.error("Invalid jar file",file.getName());
                continue;
            }
            try {
                map = (LinkedHashMap) JSONSerializer.deserialize((String) FileUtil.readFileData(false,imp));
                if (isBlank(map.get("import"))) {
                    Mod mainMod = loadClass(file,(String) map.get("main"));
                    data.add(new PluginData(map.get("name"),map.get("author"),map.get("description"),map.get("version"),mainMod));
                    dataName.add((String) map.get("name"));
                } else {
                    dataImport.add(new PluginImportData(map,file));
                }
            } catch (Exception e) {
                Log.error("Failed to load",e);
            }
        }
        for (int i=0,count=dataImport.size();i<count;i++) {
            dataImport.each(e -> {
                if (dataName.contains((String) e.pluginData.get("import"))) {
                    try {
                        Mod mainMod = loadClass(e.file, (String) e.pluginData.get("main"));
                        data.add(new PluginData(e.pluginData.get("name"),e.pluginData.get("author"),e.pluginData.get("description"),e.pluginData.get("version"),mainMod));
                        dataName.add((String) e.pluginData.get("name"));
                        dataImport.remove(e);
                    } catch (Exception err) {
                        Log.error("Failed to load", e);
                    }
                }
            });
        }
        return data;
    }

    private Mod loadClass(File file,String main) throws Exception {
        URLClassLoader classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()}, ClassLoader.getSystemClassLoader());
        Class<?> classMain = classLoader.loadClass(main);
        return (Mod) classMain.getDeclaredConstructor().newInstance();
    }

    private static class PluginImportData {
        public final LinkedHashMap pluginData;
        public final File file;

        public PluginImportData(LinkedHashMap pluginData,File file) {
            this.pluginData = pluginData;
            this.file = file;
        }
    }

    public static class PluginData {
        public final String name,author,description,version;
        public final Mod main;

        public PluginData(Object name,Object author,Object description,Object version,Mod main) {
            this.name           = (String) name;
            this.author         = (String) author;
            this.description    = (String) description;
            this.version        = (String) version;
            this.main           = main;
        }
    }
}
