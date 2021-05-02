package com.github.dr.rwserver.data.global;

import com.github.dr.rwserver.net.GroupNet;
import com.github.dr.rwserver.util.Time;

public class Static {
    public static final GroupNet groupNet = new GroupNet(Time.nanos());
}
