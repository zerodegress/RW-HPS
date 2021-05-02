package com.github.dr.rwserver.util;

import com.github.dr.rwserver.dependent.UTF8Control;
import com.github.dr.rwserver.util.log.Log;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author Dr
 * @Date ?
 */
public class LocaleUtil {

    private String[] lg = null;

    public LocaleUtil(String lg) {
        this.lg = lg.split("_");
    }

    private String language(String o,String t,String input,Object[] params) {
        Locale locale = new Locale(o,t);
        ResourceBundle bundle = ResourceBundle.getBundle("bundles/GA", locale, new UTF8Control());
        try {
            if (input !=null){
                if (params == null){
                    return bundle.getString(input);
                }else{
                    return new MessageFormat(bundle.getString(input),locale).format(params);
                }
            }
            //防止使游戏崩溃 CALL..
        } catch (MissingResourceException e) {
            Log.error(e);
        }
        return input+" : Key is invalid.";

    }

    /**
     * 传多参
     * @param      input   语言目标
     * @param      params  替换参
     * @return     文本
     */
    public String getinput(String input,Object... params) {
        if (params == null) {
            return core(input,null);
        }
        Object[] ps = new Object[params.length];
        System.arraycopy(params, 0, ps, 0, params.length);
        return core(input,ps);
    }

    /**
     * 传一数组
     * @param      input  语言目标
     * @param      ps     Object替换组
     * @return     文本
     */
    public String getinputt(String input,Object[] ps) {
        return core(input,ps);
    }

    private String core(String input,Object[] params) {
        String[] lang = lg;
        /*
        if(isBlank(lg)) {
            lang = Config.SERVER_LANGUAGE.split("_");
        }
        */
        if (params == null) {
            return language(lang[0],lang[1],input,null);
        }
        return language(lang[0],lang[1],input,params);
    }
}


