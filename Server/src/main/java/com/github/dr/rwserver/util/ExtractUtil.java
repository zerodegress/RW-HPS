package com.github.dr.rwserver.util;

import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.util.log.Log;

import java.io.UnsupportedEncodingException;

/**
 * @author Dr
 */
public class ExtractUtil {
	public static String stringToUtf8(String string) {
		try {
			// 用指定编码转换String为byte[]:
			return new String(string.getBytes("ISO-8859-1"),Data.UTF_8);
		} catch (UnsupportedEncodingException e) {
			Log.error("UTF-8",e);
			return new String(string.getBytes(),Data.UTF_8);
		}
    }

    /**
     * 合并byte数组
     */
    public static byte[] unitByteArray(byte[] byte1,byte[] byte2){
        byte[] unitByte = new byte[byte1.length + byte2.length];
        System.arraycopy(byte1, 0, unitByte, 0, byte1.length);
        System.arraycopy(byte2, 0, unitByte, byte1.length, byte2.length);
        return unitByte;
    }

	public static byte[] hexToByteArray(String inHex){
    	inHex = inHex.replace(" ","");
		int hexlen = inHex.length();
		byte[] result;
		if (hexlen % 2 != 1) {
			//偶数
			result = new byte[(hexlen/2)];
		} else {
			//奇数
			hexlen++;
			result = new byte[(hexlen/2)];
			inHex="0"+inHex;
		}
		int j=0;
		for (int i = 0; i < hexlen; i+=2){
			result[j]=(byte)Integer.parseInt(inHex.substring(i,i+2),16);
			j++;
		}
		return result;
	}

	public static String bytesToHex(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(bytes[i] & 0xFF);
			if(hex.length() < 2){
				sb.append(0);
			}
			sb.append(hex);
		}
		return sb.toString();
	}

}