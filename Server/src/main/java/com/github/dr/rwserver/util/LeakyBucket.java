package com.github.dr.rwserver.util;

/**
 * @author Dr
 */
public class LeakyBucket {
    private int water = 0;
    private final int burst;
    private long lastTime;
    private final boolean breakTrue;

    public LeakyBucket(int burst){
        this.burst=burst;
        breakTrue = (burst == 0);
    }

    /**
     * 刷新桶的水量
     */
    private void refreshWater(){
        long time = System.currentTimeMillis();
        if (time > lastTime) {
            water = 0;
            lastTime = time;
        }
    }

    /**
     * 获取令牌
     */
    public synchronized boolean tryAcquire(){
        if (breakTrue) {
            return true;
        }
        refreshWater();
        if(water<burst){
            water++;
            return true;
        }else {
            return false;
        }
    }
}
