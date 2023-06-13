/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin

import org.mozilla.javascript.Context
import org.mozilla.javascript.Function
import org.mozilla.javascript.ImporterTopLevel

/**
 * 加载 JavaScript 脚本并注入依赖
 *
 * @author RW-HPS/Dr
 */
object JavaScriptPlugin {
    fun loadJavaScriptPlugin(script: String): Plugin {
        val context = Context.enter()
        try {
            val scope = ImporterTopLevel(context)
            context.evaluateString(scope, script, "script.js", 1, null)
            val mainFunc = scope.get("main", scope)
            if (mainFunc is Function) {
                val result = (mainFunc as Function).call(context, scope, scope, null)
                return Context.jsToJava(result, Plugin::class.java) as Plugin
            } else {
                throw Exception("main function is not truly a function")
            }
        } finally {
            Context.exit()
        }
    }
}