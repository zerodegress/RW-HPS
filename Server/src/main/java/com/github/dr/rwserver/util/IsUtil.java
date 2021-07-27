package com.github.dr.rwserver.util;

import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Dr
 */
public class IsUtil {

	private static final Pattern PATTERN = Pattern.compile("[0-9]*");
	private static final Pattern IPV4_PATTERN = Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");
	private static final int IPV4_MAX_OCTET_VALUE = 255;
	

    public static boolean isBlank(Object string) {
		return string == null || "".equals(string.toString().trim());
	}

    public static boolean notIsBlank(Object string) {
		return !isBlank(string);
	}

	public static String isBlankDefaultResult(Object string) {
    	return isBlank(string) ? "" : string.toString();
	}

	public static boolean isNumeric(@NotNull final String string) {
		return PATTERN.matcher(string).matches();
	}


    public static boolean notIsNumeric(@NotNull final String string) {
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

	public static boolean isDomainName(@NotNull final String domain) {
    	try {
			isIPv4Address(InetAddress.getByName(domain).getHostAddress());
			return true;
		} catch (Exception e) {
    		return false;
		}
	}

	public static boolean isIPv4Address(@NotNull final String name) {
		final Matcher m = IPV4_PATTERN.matcher(name);
		if (!m.matches() || m.groupCount() != 4) {
			return false;
		}

		// 验证地址子组是否合法
		for (int i = 1; i <= 4; i++) {
			final String ipSegment = m.group(i);
			final int iIpSegment = Integer.parseInt(ipSegment);
			if (iIpSegment > IPV4_MAX_OCTET_VALUE) {
				return false;
			}

			if (ipSegment.length() > 1 && ipSegment.startsWith("0")) {
				return false;
			}

		}

		return true;
	}

	public static long doubleToLong(double d) {
    	return Double.doubleToLongBits(d);
	}
}