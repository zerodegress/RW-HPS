package com.github.dr.rwserver.util.io;

import java.io.Closeable;

public class Streams {
    /** Close and ignore all errors. */
    public static void close(Closeable c){
        if(c != null){
            try{
                c.close();
            }catch(Throwable ignored){
            }
        }
    }
}
