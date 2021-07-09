package com.github.dr.rwserver.util.threads;

import java.util.concurrent.*;

public class GetNewThredPool {
    public static ThreadPoolExecutor getNewFixedThreadPool(int nThreads, String name) {
        return new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),ThreadFactoryName.nameThreadFactory(name));
    }

    public static ScheduledExecutorService getNewScheduledThreadPool(int corePoolSize, String name) {
        return new ScheduledThreadPoolExecutor(corePoolSize,ThreadFactoryName.nameThreadFactory(name));
    }

    public static ScheduledThreadPoolExecutorDynamic getNewScheduledThreadDynamicPool(int corePoolSize, String name) {
        return new ScheduledThreadPoolExecutorDynamic(corePoolSize,ThreadFactoryName.nameThreadFactory(name));
    }
}
