/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.dependent

import java.lang.instrument.Instrumentation

/**
 * Agent Core
 * @author RW-HPS/Dr
 */
open class AgentAttachData {
    protected val instrumentation: Instrumentation = instPrivate!!

    companion object {
        private var instPrivate: Instrumentation? = null

        /** JRE将在启动main()之前调用方法  */
        @JvmStatic
        fun agentmain(name: String?, inst: Instrumentation?) {
            instPrivate = inst
        }
    }
}