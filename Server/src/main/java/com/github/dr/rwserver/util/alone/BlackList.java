package com.github.dr.rwserver.util.alone;

import com.github.dr.rwserver.core.ex.Threads;
import com.github.dr.rwserver.struct.Seq;

import java.util.concurrent.TimeUnit;

import static com.github.dr.rwserver.util.DateUtil.getLocalTimeFromU;

public class BlackList {
    private final Seq<BlackData> blackList = new Seq<BlackData>(false,16);

    public BlackList() {
        Threads.newThreadService2(() -> {
            final long time = getLocalTimeFromU();
            blackList.each(e -> (e.time<time),b -> blackList.remove(b));
        },0,1, TimeUnit.HOURS);
    }

    public void addBlackList(String str) {
        blackList.add(new BlackData(str,getLocalTimeFromU(3600)));
    }

    public boolean containsBlackList(String str) {
        final boolean[] result = new boolean[1];
        blackList.each(e -> {
            if (e.object.equals(str)) {
                result[0] = true;
                return;
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
            return object.equals(obj.toString());
        }
    }
}
