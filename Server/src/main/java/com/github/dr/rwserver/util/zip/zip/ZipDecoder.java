package com.github.dr.rwserver.util.zip.zip;

import com.github.dr.rwserver.game.GameMaps;
import com.github.dr.rwserver.struct.OrderedMap;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.log.Log;
import com.github.dr.rwserver.util.zip.zip.realization.ZipEntry;
import com.github.dr.rwserver.util.zip.zip.realization.ZipFile;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;


/**
 * 解码
 * @author Dr
 */
public class ZipDecoder {
    private ZipFile zipFile;
    private File file;

    public ByteArrayInputStream buffer;
    public DataInputStream stream;

    public ZipDecoder(File file) throws IOException {
        zipFile = new ZipFile(file);
        this.file = file;
    }

    public ZipDecoder(ZipFile zipFile) {
        this.zipFile = zipFile;
    }

    public ZipDecoder(byte[] bytes) {
        this.buffer = new ByteArrayInputStream(bytes);
    }

    public ZipDecoder(boolean bl,byte[] bytes) throws IOException {
        this.buffer = new ByteArrayInputStream(bytes);
        if (bl) {
            this.stream = new DataInputStream((InputStream)new BufferedInputStream((InputStream)new GZIPInputStream((InputStream)this.buffer)));
        } else {
            this.stream = new DataInputStream((InputStream)this.buffer);
        }
    }

    public OrderedMap<String, byte[]> getSpecifiedSuffixInThePackage(String endWith) {
        final OrderedMap<String,byte[]> data = new OrderedMap<>(8);
        try(ZipInputStream zis = new ZipInputStream(new FileInputStream(file), Charset.forName("GBK"))) {
            ZipEntry zipEntry;
            int len;
            InputStream in;
            final byte[] buffer = new byte[1024];
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                for(Enumeration entries = zipFile.getEntries();entries.hasMoreElements();){
                    zipEntry = (ZipEntry)entries.nextElement();
                    in  =  zipFile.getInputStream(zipEntry);
                    while ((len = in.read(buffer)) != -1) {
                        byteArrayOutputStream.write(buffer, 0, len);
                    }
                    final String name = zipEntry.getName();
                    if (name.endsWith(endWith)) {
                        data.put(name.substring(0, name.length()-name.substring(name.lastIndexOf(".")).length()),byteArrayOutputStream.toByteArray());
                        //FileUtil.File(Data.Plugin_Cache_Path).toPath(name).writeFileByte(byteArrayOutputStream.toByteArray(),false);
                    }
                    byteArrayOutputStream.reset();
                }
            }
        } catch (IOException e) {
            Log.error(e);
        }
        return data;
    }

    public Seq<String> GetTheFileNameOfTheSpecifiedSuffixInTheZip(String endWith) {
        // Max 5M
        final int maxSize = 1024 * 1024 * 5;
        final Seq<String> data = new Seq<>(8);
        ZipEntry zipEntry;
        for(Enumeration entries = zipFile.getEntries();entries.hasMoreElements();){
            zipEntry = (ZipEntry)entries.nextElement();
            if (zipEntry.getSize() >= maxSize) {
                continue;
            }
            final String name = zipEntry.getName();
            if (name.endsWith(endWith)) {
                data.add(name.substring(0, name.length()-name.substring(name.lastIndexOf(".")).length()));
            }
        }
        return data;
    }

    public byte[] GetTheFileBytesOfTheSpecifiedSuffixInTheZip(final GameMaps.MapData mapData) {
        try(ZipInputStream zis = new ZipInputStream(new FileInputStream(file), Charset.forName("GBK"))) {
            ZipEntry zipEntry;
            int len;
            InputStream in;
            final byte[] buffer = new byte[1024];
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                for(Enumeration entries = zipFile.getEntries();entries.hasMoreElements();){
                    zipEntry = (ZipEntry)entries.nextElement();
                    final String name = zipEntry.getName();
                    if (name.endsWith(mapData.getType()) && name.contains(mapData.mapFileName)) {
                        in  =  zipFile.getInputStream(zipEntry);
                        while ((len = in.read(buffer)) != -1) {
                            byteArrayOutputStream.write(buffer, 0, len);
                        }
                        return byteArrayOutputStream.toByteArray();
                    }
                }
            }
        } catch (IOException e) {
            Log.error(e);
        }
        return null;
    }

    public InputStreamReader getZipNameInputStream(String name) {
        try {
            @SuppressWarnings("unchecked")
            Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zipFile.getEntries();
            ZipEntry ze;
            while (entries.hasMoreElements()) {
                ze = entries.nextElement();
                if (ze.isDirectory()) {
                    continue;
                } else {
                    if (ze.getName() != null) {
                        if (ze.getName().equals(name)) {
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

    public static InputStream getZipInputStream(InputStream in) throws Exception {
        return (InputStream)new BufferedInputStream((InputStream)new ZipInputStream(in));
    }
}
