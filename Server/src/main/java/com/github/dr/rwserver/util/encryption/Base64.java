package com.github.dr.rwserver.util.encryption;

import com.github.dr.rwserver.data.global.Data;

public class Base64 {
    /**
     *
     * @param str Base64字符串
     * @return 解密后
     */
    public static byte[] decode(String str){
        return java.util.Base64.getDecoder().decode(str);
    }

    public static String decodeString(String str){
        return new String(java.util.Base64.getDecoder().decode(str), Data.UTF_8);
    }
    /**
     *
     * @return 加密后
     */
    public static String encode(byte[] bytes){
        return java.util.Base64.getEncoder().encodeToString(bytes);
    }

    public static String encode(String str){
        return java.util.Base64.getEncoder().encodeToString(str.getBytes(Data.UTF_8));
    }

    public static boolean isBase64(String val) {
        try {
            byte[] key= java.util.Base64.getDecoder().decode(val);
            String strs=new String(key);
            String result= java.util.Base64.getEncoder().encodeToString(strs.getBytes());
            if(result.equalsIgnoreCase(val)) {
                return true;
            }
        } catch(Exception e){
        }
        return false;
    }
}