/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.command;

import cn.rwhps.server.data.global.Data;
import cn.rwhps.server.data.global.NetStaticData;
import cn.rwhps.server.data.global.Relay;
import cn.rwhps.server.func.StrCons;
import cn.rwhps.server.net.core.server.AbstractNetConnect;
import cn.rwhps.server.util.IpUtil;
import cn.rwhps.server.util.IsUtil;
import cn.rwhps.server.util.file.FileUtil;
import cn.rwhps.server.util.game.CommandHandler;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author RW-HPS/Dr
 */
public class LogCommands {
	public LogCommands(CommandHandler handler) {
    }
}