package com.github.dr.rwserver.util.zip.gzip;

import com.github.dr.rwserver.util.log.Log;

import java.io.*;
import java.util.zip.GZIPOutputStream;

import static com.github.dr.rwserver.util.IsUtil.notIsBlank;

/**
 * @author Dr
 */
public class GzipEncoder {
	public String str;
    public final ByteArrayOutputStream buffer;
    public DataOutputStream stream;
    private GZIPOutputStream gzip = null;

    public GzipEncoder(boolean bl){
        this.buffer = new ByteArrayOutputStream();
        if (bl) {
            try {
                this.gzip = new GZIPOutputStream(this.buffer);
                this.stream = new DataOutputStream(new BufferedOutputStream(gzip));
            } catch (IOException e) {
                Log.error("GZIP Error",e);
                //TODO
            }
        } else {
			this.stream = new DataOutputStream(this.buffer);
        }	
    }

    public static OutputStream getGzipOutputStream(OutputStream out) throws Exception {
        return new BufferedOutputStream(new GZIPOutputStream(out));
    }

    public static GzipEncoder getGzipStream(String key,boolean bl) {
        GzipEncoder enc = new GzipEncoder(bl);
        enc.str = key;
        return enc;
    }

    public void closeGzip() {
        try {
            this.stream.flush();
            this.buffer.flush();
            if (notIsBlank(gzip)) {
                gzip.close();
            }
        } catch (Exception e) {
            Log.error("Close Gzip",e);
        }  
    }
}