/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */
package com.github.dr.rwserver.custom

import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.json.Json
import com.github.dr.rwserver.game.EventType.PlayerConnectPasswdCheckEvent
import com.github.dr.rwserver.net.HttpRequestOkHttp
import com.github.dr.rwserver.util.IsUtil.isBlank
import com.github.dr.rwserver.util.encryption.Md5.md5
import com.github.dr.rwserver.util.game.Events
import com.github.dr.rwserver.util.log.Log.debug
import java.io.IOException
import java.util.*

/**
 * @author Dr
 */
class CustomEvent {
}