package com.github.dr.rwserver.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Dr
 */
public class Time {
    /** 高并发下的效率提升 */
    private static final CurrentTimeMillisClock INSTANCE = new CurrentTimeMillisClock();

    /** @return 系统计时器的当前值，以纳秒为单位. */
    public static long nanos(){
        return System.nanoTime();
    }

    /** @return 当前时间与1970年1月1日午夜之间的差值（以毫秒为单位）. */
    public static long millis(){
        return System.currentTimeMillis();
    }

    /** @return 当前时间与1970年1月1日午夜之间的差值（以毫秒为单位）. */
    public static long concurrentMillis(){
        return INSTANCE.now;
    }

    /** @return 当前时间与1970年1月1日午夜之间的差值（以秒为单位）. */
    public static int concurrentSecond(){
        return (int) (INSTANCE.now/1000);
    }

    /**
     * 获取自上次以来经过的纳秒数
     * @param prevTime - 必须是纳秒
     * @return - 自prevTime以来经过的时间（以纳秒为单位）
     */
    public static long getTimeSinceNanos(final long prevTime){
        return nanos() - prevTime;
    }

    /**
     * 获取自上次以来经过的毫秒数
     * @param prevTime - 必须是毫秒
     * @return - 自prevTime以来经过的时间（以毫秒为单位）
     */
    public static long getTimeSinceMillis(final long prevTime){
        return millis() - prevTime;
    }

    public static long getTimeFutureMillis(final long addTime){
        return millis() + addTime;
    }

    public static long getUtcMillis() {
        return getUtcTime();
    }

    public static String getUtcMilliFormat(int fot) {
        return format(getUtcTime(),fot);
    }

    private static String format(final long gmt, final int fot){
        String[] ft=new String[]{"yyyy-MM-dd","yyyy-MM-dd HH:mm:ss","yyyy-MM-dd'T'HH:mm:ss'Z'","dd-MM-yyyy HH:mm:ss","MM-dd-yyyy HH:mm:ss"};
        return new SimpleDateFormat(ft[fot]).format(new Date(gmt));
    }

    private static long getUtcTime() {
        // 获取JDK当前时间
        Calendar cal = Calendar.getInstance();
        // 取得时间偏移量
        final int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
        // 取得夏令时差
        final int dstOffset = cal.get(Calendar.DST_OFFSET);
        // 从本地时间里扣除这些差量，即可以取得UTC时间
        cal.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        return cal.getTimeInMillis();
    }

    private static class CurrentTimeMillisClock {
        private volatile long now;

        private CurrentTimeMillisClock() {
            this.now = System.currentTimeMillis();
            scheduleTick();
        }

        private void scheduleTick() {
            new ScheduledThreadPoolExecutor(1, runnable -> {
                Thread thread = new Thread(runnable, "current-time-millis");
                thread.setDaemon(true);
                return thread;
            }).scheduleAtFixedRate(() -> now = System.currentTimeMillis(), 100, 100, TimeUnit.MILLISECONDS);
        }
    }
}
