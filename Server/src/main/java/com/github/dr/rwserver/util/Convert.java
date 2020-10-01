package com.github.dr.rwserver.util;

import com.github.dr.rwserver.struct.Seq;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dr
 */
public class Convert {
    public static <T> List<T> castList(Object obj, Class<T> clazz) {
        List<T> result = new ArrayList<T>();
        if(obj instanceof List<?>) {
            for (Object o : (List<?>) obj) {
                result.add(clazz.cast(o));
            }
            return result;
        }
        return null;
    }

    public static <T> Seq<T> castSeq(Object obj, Class<T> clazz) {
        Seq<T> result = new Seq<T>();
        if(obj instanceof Seq<?>) {
            for (Object o : (Seq<?>) obj) {
                result.add(clazz.cast(o));
            }
            return result;
        }
        return null;
    }
}
