package com.github.dr.rwserver.data.plugin

/**
 * Plugin的Data代理
 * @author Dr
 */
internal class Value<T>(var data: T) {
    protected fun setData(data: T): Value<T> {
        this.data = data
        return this
    }
}