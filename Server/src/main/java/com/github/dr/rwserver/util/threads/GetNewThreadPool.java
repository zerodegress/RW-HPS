package com.github.dr.rwserver.util.threads;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;

/**
 * @author Dr
 */
public class GetNewThreadPool {
    @NotNull
    public static ThreadPoolExecutor getNewFixedThreadPool(final int nThreads, @NotNull final String name) {
        return new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),ThreadFactoryName.nameThreadFactory(name));
    }

    @NotNull
    public static ScheduledExecutorService getNewScheduledThreadPool(final int corePoolSize ,@NotNull final String name) {
        return new ScheduledThreadPoolExecutor(corePoolSize,ThreadFactoryName.nameThreadFactory(name));
    }
}
