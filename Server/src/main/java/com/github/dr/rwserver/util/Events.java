package com.github.dr.rwserver.util;

import com.github.dr.rwserver.struct.ObjectMap;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.func.Cons;

/**
 * @author Dr
 */
@SuppressWarnings("unchecked")
public class Events {
    private static final ObjectMap<Object, Seq<Cons<?>>> events = new ObjectMap<>();

    public static <T> void on(Class<T> type, com.github.dr.rwserver.func.Cons<T> listener) {
        events.addGet(type, new Seq<Cons<?>>()).add(listener);
    }

    public static void on(Object type, Runnable listener) {
        events.addGet(type, new Seq<Cons<?>>()).add(e -> listener.run());
    }

    public static <T> void remove(Class<T> type, com.github.dr.rwserver.func.Cons<T> listener) {
        events.addGet(type, new Seq<Cons<?>>()).remove(listener);
    }

    public static <T> void fire(T type) {
        fire(type.getClass(), type);
    }

    public static <T> void fire(Class<?> ctype, T type) {
        if(events.get(type) != null) {
            events.get(type).each(e -> ((com.github.dr.rwserver.func.Cons<T>)e).get(type));
        }
        if(events.get(ctype) != null) {
            events.get(ctype).each(e -> ((com.github.dr.rwserver.func.Cons<T>)e).get(type));
        }
    }

    public static void dispose() {
        events.clear();
    }
}