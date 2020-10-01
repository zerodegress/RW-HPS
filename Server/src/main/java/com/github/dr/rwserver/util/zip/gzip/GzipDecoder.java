package com.github.dr.rwserver.util.zip.gzip;

import java.io.*;
import java.util.zip.*;

/**
 * @author Dr
 */
public class GzipDecoder {
    public ByteArrayInputStream buffer;
    public DataInputStream stream;

    public GzipDecoder(boolean bl,byte[] bytes) throws IOException {
    	this.buffer = new ByteArrayInputStream(bytes);
    	if (bl) {
    		this.stream = new DataInputStream((InputStream)new BufferedInputStream((InputStream)new GZIPInputStream((InputStream)this.buffer)));
    	} else {
    		this.stream = new DataInputStream((InputStream)this.buffer);
    	}
    }

    public static InputStream getGzipInputStream(InputStream in) throws Exception {
    	return (InputStream)new BufferedInputStream((InputStream)new GZIPInputStream(in));
	}
}
