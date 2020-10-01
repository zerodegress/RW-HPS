package com.github.dr.rwserver.util.zip.gzip;

import com.github.dr.rwserver.util.log.Log;

import java.io.*;
import java.util.zip.GZIPOutputStream;

/**
 * @author Dr
 */
public class GzipEncoder {
	public String str;
    public ByteArrayOutputStream buffer;
    public DataOutputStream stream;
    private GZIPOutputStream gzip;

    public GzipEncoder(boolean bl){
        this.buffer = new ByteArrayOutputStream();
        if (bl) {
            try {
                this.gzip = new GZIPOutputStream((OutputStream)this.buffer);
                this.stream = new DataOutputStream((OutputStream)new BufferedOutputStream((OutputStream) gzip));
            } catch (IOException e) {
                Log.error("GZIP Error",e);
                //TODO
            }
        } else {
			this.stream = new DataOutputStream((OutputStream)this.buffer);
        }	
    }

    public static OutputStream getGzipOutputStream(OutputStream out) throws Exception {
        return (OutputStream)new BufferedOutputStream((OutputStream)new GZIPOutputStream(out));
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
            gzip.close();
        } catch (Exception e) {
            Log.error("Close Gzip",e);
        }  
    }
}
