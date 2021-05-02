package com.github.dr.rwserver.util.game;

import com.github.dr.rwserver.func.Cons;
import com.github.dr.rwserver.struct.ObjectMap;
import com.github.dr.rwserver.struct.Seq;

/**
 * @author Dr
 */
@SuppressWarnings("unchecked")
public class Events {
    private static final ObjectMap<Object, Seq<Cons<?>>> EVENTS = new ObjectMap<>();

    public static <T> void on(Class<T> type, com.github.dr.rwserver.func.Cons<T> listener) {
        EVENTS.get(type, Seq::new).add(listener);
    }

    public static void on(Object type, Runnable listener) {
        EVENTS.get(type, Seq::new).add(e -> listener.run());
    }

    public static <T> void remove(Class<T> type, com.github.dr.rwserver.func.Cons<T> listener) {
        EVENTS.get(type, Seq::new).remove(listener);
    }

    public static <T> void fire(T type) {
        fire(type.getClass(), type);
    }

    public static <T> void fire(Class<?> type1, T type) {
        if(EVENTS.get(type) != null) {
            EVENTS.get(type).each(e -> ((com.github.dr.rwserver.func.Cons<T>)e).get(type));
        }
        if(EVENTS.get(type1) != null) {
            EVENTS.get(type1).each(e -> ((com.github.dr.rwserver.func.Cons<T>)e).get(type));
        }
    }

}