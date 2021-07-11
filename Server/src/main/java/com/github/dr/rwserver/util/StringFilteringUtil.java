package com.github.dr.rwserver.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
//Java

/**
 * @author Dr
 */
public class StringFilteringUtil {

	final static Pattern pattern = Pattern.compile("[1-9][0-9]{4,14}");

	private static String findFristGroup(Matcher matcher) {
		matcher.find();
		return matcher.group(0);
	}

	public static String removeAllisBlank(String s){
		String result = "";
		if(null!=s && !"".equals(s)){
			result = s.replaceAll("[　*| *| *|//s*]*", "");
		}
		return result;
	}

	public static String trim(String s){
		String result = "";
		if(null!=s && !"".equals(s)){
			result = s.replaceAll("^[　*| *| *|//s*]*", "").replaceAll("[　*| *| *|//s*]*$", "");
		}
		return result;
	}


    public static String removeAllEn(String s){
		String result = "";
		if(null!=s && !"".equals(s)){
			result = s.replaceAll("[^(A-Za-z)]", "");
		}
		return result;
	}


    public static String removeAllCn(String s){
		String result = "";
		if(null!=s && !"".equals(s)){
			result = s.replaceAll("[^(\\u4e00-\\u9fa5)]", "");
		}
		return result;
	}

	public static String readQQ(String str) {
		Matcher matcher = pattern.matcher(str);
		if (matcher.find()) {
			return matcher.group(0);
		}
		return "";
	}

	public static String cutting(String str,int length) {
	    if (str.length() < length) {
			return str;
		}
	    return str.substring(0, length);
	}
	public static final class StringMatcherData {
		private final Matcher matcher;

		public StringMatcherData(String patternString,String text) {
			this.matcher = Pattern.compile(patternString).matcher(text);
		}

		public String getString(int position) {
			final String result = matcher.group(position);
			return IsUtil.notIsBlank(result) ? result : "";
		}

		public int getInt(int position) {
			final String result = matcher.group(position);
			return IsUtil.notIsBlank(result) ? Integer.parseInt(result) : 0;
		}

		public String getStringNoError(int position) {
			try {
				final String result = matcher.group(position);
				return IsUtil.notIsBlank(result) ? result : "";
			} catch (Exception e) {}
			return "";
		}
	}
}