/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin

import net.rwhps.server.data.global.Data
import org.openjdk.nashorn.api.scripting.NashornScriptEngine
import javax.script.ScriptEngineManager

/**
 * 加载 JavaScript 脚本并注入依赖
 *
 * @author RW-HPS/Dr
 */
object JavaScriptPlugin {
    private val lib = """
        // ImportClass
        Object.defineProperty(this, "importClass", {
            configurable: true, enumerable: false, writable: true,
            value: function() {
                for (var arg in arguments) {
                    var clazzName = arguments[arg];
                    // 直接跳过检测, 暴力导入包(逃避默认的Only Java[java.*] Class)
                    var clazz = Java.type(clazzName);
                    var simpleName = clazzName.substring(clazzName.lastIndexOf('.') + 1);
                    this[simpleName] = clazz;
                }
            }
        });
        
        // JavaAdapter
        Object.defineProperty(this, "JavaAdapter", {
            configurable: true, enumerable: false, writable: true,
            value: function() {
                if (arguments.length < 2) {
                    throw new TypeError("JavaAdapter requires atleast two arguments");
                }

                var types = Array.prototype.slice.call(arguments, 0, arguments.length - 1);
                var NewType = Java.extend.apply(Java, types);
                return new NewType(arguments[arguments.length - 1]);
            }
        });
        
        Object.defineProperty(this, "importPackage", {
            configurable: true, enumerable: false, writable: true,
            value: function() {
                for (var arg in arguments) {
                    Java.type("net.rwhps.server.util.file.FileScan").scanPacketPathClass(arguments[arg]).eachAll(function(className) {
                        importClass(className)
                    })
                }
            }
        });
    """.trimIndent()

    fun loadJavaScriptPlugin(script: String): Plugin {
        val manager = ScriptEngineManager()
        val engine: NashornScriptEngine = manager.getEngineByName("nashorn") as NashornScriptEngine
        engine.eval("$lib${Data.LINE_SEPARATOR}$script")
        return engine.invokeFunction("main") as Plugin
    }
}