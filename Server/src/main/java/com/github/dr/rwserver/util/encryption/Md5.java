package com.github.dr.rwserver.util.encryption;

import com.github.dr.rwserver.struct.Seq;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.github.dr.rwserver.util.IsUtil.notIsBlank;
//Java

public class Md5 {

	private static final char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd','e', 'f' };

	public static String md5(String input) {
		if (input == null) {
            return null;
        }
		try {
			byte[] resultByteArray = MessageDigest.getInstance("MD5").digest(input.getBytes("UTF-8"));
			return byteArrayToHex(resultByteArray);
		} catch (NoSuchAlgorithmException e) {
			//Log.error
		} catch (UnsupportedEncodingException e) {

		}
		return null;
	}

	public static String md5Formant(String str) {
		try {
			byte[] digest = MessageDigest.getInstance("MD5").digest(str.getBytes("UTF-8"));
			StringBuilder sb = new StringBuilder(digest.length * 2);
			for (byte b2 : digest) {
				int b3 = b2 & 0xFF;
				if (b3 < 16) {
					sb.append('0');
				}
				sb.append(Integer.toHexString(b3));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e2) {
			throw new RuntimeException("MD5 should be supported", e2);
		} catch (UnsupportedEncodingException e3) {
			throw new RuntimeException("UTF-8 should be supported", e3);
		}
	}

	public static Seq<String> md5(Seq<File> list) {
		Seq<String> result = new Seq<>();
		list.each(e -> {
			String md5 = md5(e);
			if (notIsBlank(md5)) {
				result.add(md5);
			}
		});
		return result;
	}

	public static String md5(File file) {
		try {
			if (!file.isFile()) {
                return null;
            }
			FileInputStream in = new FileInputStream(file);
			String result = md5(in);
			in.close();
			return result;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String md5(InputStream in) {
		try {
			MessageDigest messagedigest = MessageDigest.getInstance("MD5");
			byte[] buffer = new byte[1024];
			int read = 0;
			while ((read = in.read(buffer)) != -1) {
                messagedigest.update(buffer, 0, read);
            }
			in.close();
			return byteArrayToHex(messagedigest.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String byteArrayToHex(byte[] byteArray) {
		char[] resultCharArray = new char[byteArray.length * 2];
		int index = 0;
		for (byte b : byteArray) {
			resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
			resultCharArray[index++] = hexDigits[b & 0xf];
		}
		return new String(resultCharArray);

	}

}
