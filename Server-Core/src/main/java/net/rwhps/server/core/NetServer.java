/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.core;

import net.rwhps.server.core.thread.CallTimeTask;
import net.rwhps.server.core.thread.Threads;
import net.rwhps.server.data.global.Data;
import net.rwhps.server.data.global.NetStaticData;
import net.rwhps.server.util.RandomUtils;
import net.rwhps.server.util.StringFilteringUtil;
import net.rwhps.server.util.Time;
import net.rwhps.server.util.algorithms.digest.DigestUtils;
import net.rwhps.server.util.log.Log;

import java.math.BigInteger;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static net.rwhps.server.net.HttpRequestOkHttp.doPostRw;

/**
 * @author RW-HPS/Dr
 */
@SuppressWarnings("deprecation")
public class NetServer {

    static String userId;

    public static void closeServer() {
    }

    public static void reLoadServer() {
    }
}
