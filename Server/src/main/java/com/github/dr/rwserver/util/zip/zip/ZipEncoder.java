package com.github.dr.rwserver.util.zip.zip;

import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.zip.zip.realization.ZipEntry;
import com.github.dr.rwserver.util.zip.zip.realization.ZipFile;
import com.github.dr.rwserver.util.zip.zip.realization.ZipOutputStream;

import java.io.*;
import java.util.Enumeration;
import java.util.HashSet;

/**
 * @author Dr
 */
public class ZipEncoder {
	/**
     * 合并压缩文件
     * @param sourceZipFiles in
     * @return byte out
     * @throws IOException err
     */
    public static byte[] incrementalUpdate(Seq<String> updateFile, String... sourceZipFiles) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ZipOutputStream out =new ZipOutputStream(outputStream)){
            HashSet<String> names = new HashSet<>();
            for(String sourceZipFile : sourceZipFiles){
                try (ZipFile zipFile = new ZipFile(sourceZipFile)) {
                    ZipEntry ze;
                    Enumeration<? extends ZipEntry> enumeration = zipFile.getEntries();
                    while (enumeration.hasMoreElements()) {
                        ze = enumeration.nextElement();
                        if (!ze.isDirectory()) {
                            /* 只合并第一个源压缩包里面的文件，后面若有相同的文件则跳过执行合并 */
                            if (names.contains(ze.getName()) || !updateFile.contains(ze.getName())) {
                                continue;
                            }
                            ZipEntry oze = new ZipEntry(ze.getName());
                            out.putNextEntry(oze);
                            if (ze.getSize() > 0) {
                                DataInputStream dis = new DataInputStream(zipFile.getInputStream(ze));
                                int len;
                                byte[] bytes = new byte[1024];
                                while ((len = dis.read(bytes)) > 0) {
                                    out.write(bytes, 0, len);
                                }
                                out.closeEntry();
                                out.flush();
                            }
                            names.add(oze.getName());
                        }
                    }
                }
            }
        }
        return outputStream.toByteArray();
    }
}