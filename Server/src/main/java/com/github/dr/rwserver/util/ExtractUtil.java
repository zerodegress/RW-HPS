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

	public static byte[] hexToByteArray(String inHex){
		int hexlen = inHex.length();
		byte[] result;
		if (hexlen % 2 == 1){
			//奇数
			hexlen++;
			result = new byte[(hexlen/2)];
			inHex="0"+inHex;
		}else {
			//偶数
			result = new byte[(hexlen/2)];
		}
		int j=0;
		for (int i = 0; i < hexlen; i+=2){
			result[j]=hexToByte(inHex.substring(i,i+2));
			j++;
		}
		return result;
	}

	private static byte hexToByte(String inHex){
		return (byte)Integer.parseInt(inHex,16);
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