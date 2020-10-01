package com.github.dr.rwserver.util.encryption;

/**
 * @author Dr.
 * @Data 2020/6/25 9:28
 */
public class Game {
	public static String connectak(int paramInt) {
		StringBuffer ak = new StringBuffer(16);
		ak.append("c:" + paramInt)
			  .append("m:" + (paramInt * 87 + 24))
			  .append("0:" + (44000 * paramInt))
			  .append("1:" + paramInt)
			  .append("2:" + (13000 * paramInt))
			  .append("3:" + (28000 + paramInt))
			  .append("4:" + (75000 * paramInt))
			  .append("5:" + (160000 + paramInt))
			  .append("6:" + (850000 * paramInt))
			  .append("t1:" + (44000 * paramInt))
			  .append("d:" + (5 * paramInt));
	    return ak.toString();
  	}
}