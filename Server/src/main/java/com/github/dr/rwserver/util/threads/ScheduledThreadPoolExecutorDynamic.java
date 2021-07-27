package com.github.dr.rwserver.util.threads;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;


/**
 * 动态改变线程池的线程数量
 * @author Dr
 */
@Deprecated(forRemoval = true)
public class ScheduledThreadPoolExecutorDynamic extends ScheduledThreadPoolExecutor {

    public ScheduledThreadPoolExecutorDynamic(int corePoolSize) {
        super(corePoolSize);
    }

    public ScheduledThreadPoolExecutorDynamic(int corePoolSize, @NotNull final ThreadFactory threadFactory) {
        super(corePoolSize,threadFactory);
    }

    @NotNull
    @Override
    public ScheduledFuture<?> schedule(@NotNull final Runnable command, long end, @NotNull final TimeUnit unit) {
        dynamicCorePoolSize(this);
        return super.schedule(command, end, unit);
    }

    @NotNull
    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(@NotNull final Runnable command, long initialDelay, long period, @NotNull final TimeUnit unit) {
        dynamicCorePoolSize(this);
        return super.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    public boolean cancelSchedule(@NotNull final ScheduledFuture<?> scheduledFuture) {
        if (!scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(true);
            this.setCorePoolSize(this.getCorePoolSize() - 1);
            return true;
        }
        return false;
    }

    private static void dynamicCorePoolSize(@NotNull final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor) {
        int activeCount = scheduledThreadPoolExecutor.getActiveCount();
        int corePoolSize = scheduledThreadPoolExecutor.getCorePoolSize();
        if (activeCount == corePoolSize) {
            scheduledThreadPoolExecutor.setCorePoolSize(corePoolSize + 1);
        } else if (activeCount < corePoolSize - 1) {
            scheduledThreadPoolExecutor.setCorePoolSize(corePoolSize - 1);
        }
    }
}