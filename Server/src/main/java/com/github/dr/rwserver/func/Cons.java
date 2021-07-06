package com.github.dr.rwserver.func;

/**
 * Cons 轮循 Seq专用
 * @author Dr
 */
public interface Cons<T>{
    /**
     * echo -> .get(i)
     * @param t 内容
     */
    void get(T t);
}
