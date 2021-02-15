package com.github.dr.rwserver.util.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;

import static com.github.dr.rwserver.data.global.Data.LINE_SEPARATOR;
import static com.github.dr.rwserver.util.DateUtil.getLocalTimeFromU;

/**
 * Log Util
 * @author Dr
 * @version 1.1
 * @date 2020年3月8日星期日 3:54  
 * 练手轮子? :P 
 */

@SuppressWarnings("unused")
public class Log {
	/** 默认 WARN */
	private static int LOG_GRADE = 5;
	private static LogPrint<Object> logPrint;
	private static final StringBuilder LOG_CACHE = new StringBuilder();


	private enum Logg {
		 /* Log等级 默认为WARN */
		 /* 开发时为ALL */
		OFF(8),FATAL(7),ERROR(6),WARN(5),INFO(4),DEBUG(3),TRACE(2),ALL(1);
		private final int num;
		Logg(int num) {
			this.num=num;
		}
		private int getLogg() {
			return num;
		}
	}

	public static void set(String log) {
		Log.LOG_GRADE=Logg.valueOf(log).getLogg();
	}

	public static void setPrint(boolean system) {
		logPrint =  system ? System.out::println : LOG_CACHE::append;
	}

	public static String getLogCache() {
		String result = LOG_CACHE.toString();
		LOG_CACHE.delete(0,LOG_CACHE.length());
		return result;
	}

	/**
	 * Log：
	 * tag 标题 默认警告级
	 */

	public static void skipping(Object e) {
		logs(9,"SKIPPING",e);
	}
	public static void skipping(Object tag, Object e) {
		logs(9,tag,e);
	}
	public static void fatal(Exception e) {
		log(7,"FATAL",e);
	}
	public static void fatal(Object tag, Exception e) {
		log(7,tag,e);
	}
	public static void fatal(Object e) {
		logs(7,"FATAL",e);
	}
	public static void fatal(Object tag, Object e) {
		logs(7,tag,e);
	}
	public static void error(Exception e) {
		log(6,"ERROR",e);
	}
	public static void error(Object tag, Exception e) {
		log(6,tag,e);
	}
	public static void error(Object e) {
		logs(6,"ERROR",e);
	}
	public static void error(Object tag, Object e) {
		logs(6,tag,e);
	}

	public static void warn(Exception e) {
		log(5,"WARN",e);
	}
	public static void warn(Object tag, Exception e) {
		log(5,tag,e);
	}
	public static void warn(Object e) {
		logs(5,"WARN",e);
	}
	public static void warn(Object tag, Object e) {
		logs(5,tag,e);
	}

	public static void info(Exception e) {
		log(4,"INFO",e);
	}
	public static void info(Object tag, Exception e) {
		log(4,tag,e);
	}
	public static void info(Object e) {
		logs(4,"INFO",e);
	}
	public static void info(Object tag, Object e) {
		logs(4,tag,e);
	}

	public static void debug(Exception e) {
		log(3,"DEBUG",e);
	}
	public static void debug(Object tag, Exception e) {
		log(3,tag,e);
	}
	public static void debug(Object e) {
		logs(3,"DEBUG",e);
	}
	public static void debug(Object tag, Object e) {
		logs(3,tag,e);
	}

	public static void track(Exception e) {
		log(2,"TRACK",e);
	}
	public static void track(Object tag, Exception e) {
		log(2,tag,e);
	}


	public static String logs(Exception e) {
		final StringWriter stringWriter = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);
		return stringWriter.getBuffer().toString();
	}

	/**
	 * WLog：
	 * @param i 警告级
	 * @param tag 标题 默认警告级
	 * @param e Exception
	 *i>=设置级 即写入文件
	 */
	private static void log(int i, Object tag, Exception e) {
		final StringWriter stringWriter = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);
		logs(i,tag,stringWriter.getBuffer());
	}


	private static void logs(int i, Object tag, Object e) {
		if(LOG_GRADE>i) {
			return;
		}
		final StringBuilder sb = new StringBuilder();
		final String[] lines = e.toString().split(LINE_SEPARATOR);
		sb.append("UTC [")
			.append(getLocalTimeFromU(0,1)).append("] ")
			//.append(LINE_SEPARATOR)
			.append(tag)
			.append(": ")
			.append(LINE_SEPARATOR);
		for (Object line : lines) {
			sb.append(line)
				.append(LINE_SEPARATOR);
		}
		logPrint.println(sb);
	}

	public static void clog(String text) {
		final StringBuilder sb = new StringBuilder();
		sb.append("[")
			.append(getLocalTimeFromU(0,1))
			.append(" UTC] ")
			.append(text);
		text = sb.toString();
        System.out.println(formatColors(text+"&fr"));
	}

	public static void clog(String text,Object... obj) {
		clog(new MessageFormat(text).format(obj));
	}

	public static String formatColors(String text){
		for(int i = 0; i < ColorCodes.CODES.length; i++){
			text = text.replace("&" + ColorCodes.CODES[i], ColorCodes.VALUES[i]);
		}
		return text;
	}

	private interface LogPrint<T>{
		/**
		 * 接管Log逻辑
		 * @param t TEXT
		 */
		void println(T t);
	}


}