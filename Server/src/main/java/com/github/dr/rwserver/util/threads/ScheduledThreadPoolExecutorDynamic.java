package com.github.dr.rwserver.util.threads;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;


public class ScheduledThreadPoolExecutorDynamic extends ScheduledThreadPoolExecutor {

    public ScheduledThreadPoolExecutorDynamic(int corePoolSize) {
        super(corePoolSize);
    }

    public ScheduledThreadPoolExecutorDynamic(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize,threadFactory);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        dynamicCorePoolSize(this);
        return super.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    private static void dynamicCorePoolSize(ScheduledThreadPoolExecutor scheduledThreadPoolExecutor) {
        int activeCount = scheduledThreadPoolExecutor.getActiveCount();
        int corePoolSize = scheduledThreadPoolExecutor.getCorePoolSize();
        if (activeCount == corePoolSize) {
            scheduledThreadPoolExecutor.setCorePoolSize(corePoolSize + 1);
        } else if (activeCount < corePoolSize - 1) {
            scheduledThreadPoolExecutor.setCorePoolSize(corePoolSize - 1);
        }
    }

    public boolean cancelSchedule(ScheduledFuture<?> scheduledFuture) {
        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(true);
            this.setCorePoolSize(this.getCorePoolSize() - 1);
            return true;
        }
        return false;
    }
}