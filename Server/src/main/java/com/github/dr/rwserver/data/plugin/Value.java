package com.github.dr.rwserver.data.plugin;


/**
 * Plugin的Data代理
 * @author Dr
 */
public class Value<T> {
    private T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}