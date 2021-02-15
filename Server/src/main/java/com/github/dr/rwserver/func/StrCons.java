package com.github.dr.rwserver.func;

import java.text.MessageFormat;

/**
 * @author Dr
 */
public interface StrCons {
    /**
     * Log 专用
     * @param str String
     */
    void get(String str);

    /**
     * Log 转换
     * @param t String
     * @param obj Object...
     */
    default void get(String t,Object... obj) {
        get(new MessageFormat(t).format(obj));
    }
}
