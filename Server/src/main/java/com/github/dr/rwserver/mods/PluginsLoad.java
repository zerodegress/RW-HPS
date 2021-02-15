package com.github.dr.rwserver.mods;

import com.github.dr.rwserver.data.json.Json;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.file.FileUtil;
import com.github.dr.rwserver.util.log.Log;
import com.github.dr.rwserver.util.zip.zip.ZipDecoder;

import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import static com.github.dr.rwserver.util.IsUtil.isBlank;

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
        for (File file : jarFileList) {
            try {
                InputStreamReader imp = new ZipDecoder(file).getZipNameInputStream("plugin.json");
                if (isBlank(imp)) {
                    Log.error("Invalid jar file",file.getName());
                    continue;
                }
                Json json = new Json((String) FileUtil.readFileData(false,imp));
                if (isBlank(json.getData("import"))) {
                    Mod mainMod = loadClass(file,(String) json.getData("main"));
                    data.add(new PluginData(json.getData("name"),json.getData("author"),json.getData("description"),json.getData("version"),mainMod));
                    dataName.add((String) json.getData("name"));
                } else {
                    dataImport.add(new PluginImportData(json,file));
                }
            } catch (Exception e) {
                Log.error("Failed to load",e);
            }
        }
        for (int i=0,count=dataImport.size();i<count;i++) {
            dataImport.each(e -> {
                if (dataName.contains((String) e.pluginData.getData("import"))) {
                    try {
                        Mod mainMod = loadClass(e.file, (String) e.pluginData.getData("main"));
                        data.add(new PluginData(e.pluginData.getData("name"),e.pluginData.getData("author"),e.pluginData.getData("description"),e.pluginData.getData("version"),mainMod));
                        dataName.add((String) e.pluginData.getData("name"));
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
        public final Json pluginData;
        public final File file;

        public PluginImportData(Json pluginData,File file) {
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
