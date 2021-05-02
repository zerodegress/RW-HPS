package com.github.dr.rwserver.util.encryption;

/**
 * @author Dr.
 * @Data 2020/6/25 9:28
 */
public class Game {
	public static String connectKey(int paramInt) {
		StringBuilder ak = new StringBuilder(16);
		ak.append("c:").append(paramInt).append("m:").append(paramInt * 87 + 24).append("0:").append(44000 * paramInt).append("1:").append(paramInt).append("2:").append(13000 * paramInt).append("3:").append(28000 + paramInt).append("4:").append(75000 * paramInt).append("5:").append(160000 + paramInt).append("6:").append(850000 * paramInt).append("t1:").append(44000 * paramInt).append("d:").append(5 * paramInt);
	    return ak.toString();
  	}
}