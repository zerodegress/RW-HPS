/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

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