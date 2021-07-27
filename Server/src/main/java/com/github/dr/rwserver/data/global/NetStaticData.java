package com.github.dr.rwserver.data.global;

import com.github.dr.rwserver.net.GroupNet;
import com.github.dr.rwserver.net.game.ProtocolData;
import com.github.dr.rwserver.net.game.StartNet;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.Time;
import com.github.dr.rwserver.util.alone.BlackList;
/**
 * @author Dr
 */
public class NetStaticData {
    public static final GroupNet groupNet = new GroupNet(Time.nanos());
    public static final BlackList blackList = new BlackList();
    public static final ProtocolData protocolData = new ProtocolData();
    public static Seq<StartNet> startNet = new Seq<>(4);
}