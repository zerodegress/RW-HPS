package com.github.dr.rwserver.util;

import java.util.regex.Pattern;

/**
 * @author Dr
 */
public class IsUtil {

	private final static Pattern PATTERN = Pattern.compile("[0-9]*");
	

    public static boolean isBlank(Object string) {
		return string == null || "".equals(string.toString().trim());
	}

    public static boolean notIsBlank(Object string) {
		return !isBlank(string);
	}

	public static String isBlankDefaultResult(Object string) {
    	return isBlank(string) ? "" : string.toString();
	}

	public static boolean isNumeric(String string) {
		return PATTERN.matcher(string).matches();
	}


    public static boolean notIsNumeric(String string) {
		return !isNumeric(string);
	}

	public static boolean isTwoTimes(int n) {
		return n > 0 && (n & 1) == 0;
	}

	public static boolean isPowerOfTwo(int n) {
		return n > 0 && (n & (n-1)) == 0;
	}

	public static boolean inTwoNumbers(double min,double b,double max) {
    	return Math.max(min, b) == Math.min(b, max);
	}

	public static boolean inTwoNumbersNoSE(double min,double b,double max) {
    	if (doubleToLong(min) != doubleToLong(b) && doubleToLong(max) != doubleToLong(b)) {
    		return inTwoNumbers(min,b,max);
		}
    	return false;
	}

	public static boolean inTwoNumbersNoSrE(double min,double b,double max,boolean start) {
		if (start) {
			if (doubleToLong(max) != doubleToLong(b)) {
				return inTwoNumbers(min,b,max);
			}
		} else {
			if (doubleToLong(min) != doubleToLong(b)) {
				return inTwoNumbers(min,b,max);
			}
		}
		return false;
	}

	private static long doubleToLong(double d) {
    	return Double.doubleToLongBits(d);
	}
}