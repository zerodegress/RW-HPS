package com.github.dr.rwserver.util.file;

import com.github.dr.rwserver.util.log.Log;

import java.io.File;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Dr
 */
public class ZipFi {

    private ZipFile zipFile;

    public ZipFi(File file) {
        try {
            zipFile = new ZipFile(file);
        } catch (Exception e) {
            Log.info("ZIP", e);
        }
    }

    public InputStreamReader getZipInputStream() {
        try {
            @SuppressWarnings("unchecked")
            Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zipFile.entries();
            ZipEntry ze;
            while (entries.hasMoreElements()) {
                ze = entries.nextElement();
                if (ze.isDirectory()) {
                    continue;
                } else {
                    if (ze.getName() != null) {
                        if (ze.getName().endsWith("plugin.json")) {
                            return new InputStreamReader(zipFile.getInputStream(ze),"UTF-8");
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.error(e);
        }
        return null;
    }
}
