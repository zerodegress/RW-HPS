package com.github.dr.rwserver.util.alone;

import com.github.dr.rwserver.core.thread.Threads;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.Time;

import java.util.concurrent.TimeUnit;

public class BlackList {
    private final Seq<BlackData> blackList = new Seq<>(false, 16);

    public BlackList() {
        Threads.newThreadService2(() -> {
            final long time = Time.millis();
            blackList.each(e -> (e.time<time), blackList::remove);
        },0,1, TimeUnit.HOURS,"BlackListCheck");
    }

    public void addBlackList(String str) {
        blackList.add(new BlackData(str,Time.getTimeFutureMillis(3600 * 1000L)));
    }

    public boolean containsBlackList(String str) {
        final boolean[] result = new boolean[1];
        blackList.each(e -> {
            if (e.object.equals(str)) {
                result[0] = true;
            }
        });
        return result[0];
    }

    private static class BlackData {
        protected final String object;
        protected final long time;
        protected BlackData(String object,long time) {
            this.object = object;
            this.time = time;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            return object.equals(obj.toString());
        }
    }
}
