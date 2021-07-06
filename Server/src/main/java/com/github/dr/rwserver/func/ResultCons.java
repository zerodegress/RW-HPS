package com.github.dr.rwserver.func;

/**
 * @author Dr
 */
public interface ResultCons<T> {
    /**
     * echo -> .get(i)
     * @param t 内容
     * @return T
     */
    T get(T t);
}