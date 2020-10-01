package com.github.dr.rwserver.util.log;

public class Error {

    public static String error(String type) {
		return com.github.dr.rwserver.util.log.ErrorCode.valueOf(type).getError();
	}

    public static int code(String type) {
		return com.github.dr.rwserver.util.log.ErrorCode.valueOf(type).getCode();
	}
}