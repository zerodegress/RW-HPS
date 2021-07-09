package com.github.dr.rwserver.net.game;

import com.github.dr.rwserver.net.core.AbstractNetConnect;
import com.github.dr.rwserver.util.Time;
import com.github.dr.rwserver.util.threads.ThreadFactoryName;

import java.util.concurrent.*;

/**
 * @author Dr
 */
class TimeoutDetection {
    private final ThreadFactory namedFactory = ThreadFactoryName.nameThreadFactory("TimeoutDetection-");
    private final ScheduledExecutorService SERVICE = new ScheduledThreadPoolExecutor(1,namedFactory);
    private final ScheduledFuture scheduledFuture;

    protected TimeoutDetection(int s,final StartNet startNet) {
        scheduledFuture = SERVICE.scheduleAtFixedRate(new CheckTime(startNet),0,s, TimeUnit.SECONDS);
    }

    protected static boolean checkTimeoutDetection(final StartNet startNet, final AbstractNetConnect abstractNetConnect) {
        if (abstractNetConnect == null) {
            return true;
        }
        if (abstractNetConnect.getIsPasswd()) {
            /* 60s无反应判定close */
            return Time.concurrentMillis() > (abstractNetConnect.getLastReceivedTime() + 60 * 1000L);
        } else {
            return Time.concurrentMillis() > (abstractNetConnect.getLastReceivedTime() + 180 * 1000L);
        }
    }

    private static class CheckTime implements Runnable {
        private final StartNet startNet;

        private CheckTime(StartNet startNet) {
            this.startNet = startNet;
        }
        @Override
        public void run() {
            startNet.OVER_MAP.each((k,v) -> {
                if (checkTimeoutDetection(startNet,v)) {
                    v.disconnect();
                    startNet.OVER_MAP.remove(k);
                }
            });
        }
    }

}
