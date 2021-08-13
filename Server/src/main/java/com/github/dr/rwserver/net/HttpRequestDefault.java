/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.net;

import com.github.dr.rwserver.util.IsUtil;
import com.github.dr.rwserver.util.log.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Dr
 */
public class HttpRequestDefault {

    public static boolean downUrl(final String url,final File file) {
        HttpURLConnection conn = null;
        try{
            File filepath=file.getParentFile();
            if(!filepath.exists()) {
                filepath.mkdirs();
            }
            URL httpUrl=new URL(url);
            while (true) {
                conn = (HttpURLConnection) httpUrl.openConnection();
                conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36");
                if (conn.getResponseCode() == 301 || conn.getResponseCode() == 302) {
                    final String newUrl = conn.getHeaderField("Location");
                    if (IsUtil.isBlank(newUrl)) {
                        Log.error("Download Fail: Empty Redirect",url);
                        return false;
                    }
                    conn.disconnect();
                    httpUrl = new URL(newUrl);
                } else {
                    break;
                }
            }
            try (BufferedInputStream bis = new BufferedInputStream(conn.getInputStream())) {
                try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
                    byte[] buf = new byte[4096];
                    int length = bis.read(buf);
                    while (length != -1) {
                        bos.write(buf, 0, length);
                        length = bis.read(buf);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            Log.error("downUrl",e);
        } finally {
            if (IsUtil.notIsBlank(conn)) {
                conn.disconnect();
            }
        }
        return false;
    }
}
