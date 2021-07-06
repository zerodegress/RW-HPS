package com.github.dr.rwserver.util.threads;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Dr
 */
public class ThreadFactoryName {
    /**
     * 自定义ThreadFactory
     * @return java.util.concurrent.ThreadFactory
     **/
    public static ThreadFactory nameThreadFactory(final String name){
        AtomicInteger tag = new AtomicInteger(1);
        return (Runnable r) -> {
            Thread thread = new Thread(r);
            thread.setName(name + tag.getAndIncrement());
            return thread;
        };
    }
}
