package com.github.dr.rwserver.func;

/**
 * Boolf 轮询中if判定 Seq专用
 * @author Dr
 */
public interface Boolf<T>{
    boolean get(T t);

    default Boolf<T> and(Boolf<T> pred){
        return t -> get(t) && pred.get(t);
    }

    default Boolf<T> or(Boolf<T> pred){
        return t -> get(t) || pred.get(t);
    }
}
