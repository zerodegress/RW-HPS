package com.github.dr.rwserver.math;

/**
 * @author Dr
 */
public final class Mathf {

    public static final Rand random = new Rand();

    /** 获取2的次方. */
    public static int nextPowerOfTwo(int value) {
        if(value == 0) {
            return 1;
        }
        value--;
        value |= value >> 1;
        value |= value >> 2;
        value |= value >> 4;
        value |= value >> 8;
        value |= value >> 16;
        return value + 1;
    }

    /** 返回介于0（含）和指定值（含）之间的随机数 */
    public static int random(int range){
        return random.nextInt(range + 1);
    }

    /** 返回开始（包括）和结束（包括）之间的随机数 */
    public static int random(int start, int end){
        return start + random.nextInt(end - start + 1);
    }
}
