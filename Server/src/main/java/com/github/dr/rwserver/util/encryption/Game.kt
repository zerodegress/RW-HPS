package com.github.dr.rwserver.util.encryption

/**
 * @author Dr.
 * @Data 2020/6/25 9:28
 */
object Game {
    @JvmStatic
	fun connectKey(paramInt: Int): String {
        return "c:" + paramInt + "m:" + (paramInt * 87 + 24) + "0:" + 44000 * paramInt + "1:" + paramInt + "2:" + 13000 * paramInt + "3:" + (28000 + paramInt) + "4:" + 75000 * paramInt + "5:" + (160000 + paramInt) + "6:" + 850000 * paramInt + "t1:" + 44000 * paramInt + "d:" + 5 * paramInt
    }
}