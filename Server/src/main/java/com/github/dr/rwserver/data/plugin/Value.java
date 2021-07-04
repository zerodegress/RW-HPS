package com.github.dr.rwserver.data.plugin;


/**
 * Plugin的Data代理
 * @author Dr
 */
class Value<T> {
    private T data;

    protected Value(T data) {
        this.data = data;
    }

    protected T getData() {
        return data;
    }

    protected Value<T> setData(T data) {
        this.data = data;
        return this;
    }
}