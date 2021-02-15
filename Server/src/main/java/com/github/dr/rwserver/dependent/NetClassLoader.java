package com.github.dr.rwserver.dependent;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * @author Dr
 */
public class NetClassLoader extends ClassLoader {
    private final JarInputStream jarInputStream;
    private final Map<String, ByteBuffer> entryMap;

    public NetClassLoader(String src) throws IOException {
        this(new FileInputStream(src));
    }

    public NetClassLoader(File file) throws IOException {
        this(new FileInputStream(file));
    }

    public NetClassLoader(InputStream is) throws IOException {
        jarInputStream = new JarInputStream(is);
        entryMap = new HashMap<>();
        JarEntry entry;
        while((entry = jarInputStream.getNextJarEntry()) != null) {
            String name = entry.getName();
            byte[] bytes = getBytes(jarInputStream);
            if (name.endsWith(".class")) {
                byte[] debytes = bytes;
                ByteBuffer buffer = ByteBuffer.wrap(debytes);
                entryMap.put(name, buffer);
            } else {
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                entryMap.put(name, buffer);
            }
        }
        jarInputStream.close();
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String path = name.replace('.', '/').concat(".class");
        ByteBuffer buffer = entryMap.get(path);
        if(buffer == null) {
            return super.findClass(name);
        } else {
            byte[] bytes = buffer.array();
            return defineClass(name, bytes, 0, bytes.length);
        }
    }

    /**
     * 从jar输入流中读取信息
     * @param jarInputStream JarInputStream
     * @return bytes
     * @throws IOException err
     */
    private byte[] getBytes(JarInputStream jarInputStream) throws IOException {
        int len = 0;
        byte[] bytes = new byte[8192];
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
        while((len = jarInputStream.read(bytes, 0, bytes.length)) != -1) {
            baos.write(bytes, 0, len);
        }
        return baos.toByteArray();
    }

    /**
     * 关闭Decoder
     * @throws IOException err
     */
    public void close() throws IOException {
        for (ByteBuffer buffer : entryMap.values()) {
            buffer.clear();
        }
        entryMap.clear();
    }
}
