package net.rwhps.server.plugin

import net.rwhps.server.data.global.Data
import net.rwhps.server.data.json.Json
import net.rwhps.server.struct.ObjectMap
import net.rwhps.server.struct.OrderedMap
import net.rwhps.server.util.ExtractUtils
import net.rwhps.server.util.log.Log
import org.junit.jupiter.api.Test

/**
 * 建议使用 jslib 项目开发, 裸写 mjs 是受罪
 *
 * 什么傻逼玩意, 测试都能写半天
 *
 * @date 2023/6/21 11:00 想骂人
 * @date 2023/6/21 13:09 更想骂人
 *
 * @author RW-HPS/Dr
 */
class JavaScriptPluginTest {
    init {
        Log.set("DEBUG")
        Log.setCopyPrint(true)
    }

    @Test
    fun loadJavaScriptPlugin() {
        val scriptPluginGlobalContext = JavaScriptPluginGlobalContext()

        val data = OrderedMap<String,ByteArray>().apply {
            put("/main.mjs", ExtractUtils.bytes("""
                import {Test} from './a/../test.mjs'
                import {Plugin} from 'java:net.rwhps.server.plugin'
                import {Log} from 'java:net.rwhps.server.util.log'
                
                export default new (Java.extend(Plugin, {
                        onEnable: function() {
                            Log.debug("Hi onEnable Test-1")
                            Log.debug(new Test().square(5))
                        },
                        init: function() {
                            Log.debug("oneClass")
                        }
                    }))()
            """.trimIndent(), Data.UTF_8))

            put("/test.mjs", ExtractUtils.bytes("""
                import {Test_1} from './a/a/test.mjs'
                
                export class Test {
                    square(x) {
                        return x * x;
                    }
                }
            """.trimIndent(), Data.UTF_8))

            put("/a/a/test.mjs", ExtractUtils.bytes("""
                export class Test_1 {
                    square(x) {
                        return x * x;
                    }
                }
            """.trimIndent(), Data.UTF_8))
        }

        val data1 = OrderedMap<String,ByteArray>().apply {
            put("/main.mjs", ExtractUtils.bytes("""
                import {Test} from '/Test 1/a/../test.mjs'
                import {Plugin} from 'java:net.rwhps.server.plugin'
                import {Log} from 'java:net.rwhps.server.util.log'
                
                export default new (Java.extend(Plugin, {
                        onEnable: function() {
                            Log.debug("Hi onEnable Test-2")
                            Log.debug(new Test().square(5))
                        },
                        init: function() {
                            Log.debug("oneClass")
                        }
                    }))()
            """.trimIndent(), Data.UTF_8))
        }
        // 这个是模块化, 可以使用import/export
        scriptPluginGlobalContext.addESMPlugin(Json(ObjectMap.of<String,String>(
            "name", "Test 1",
            "author", "",
            "description", "",
            "main", "main.mjs",
            "version", ""
        )), data)
        scriptPluginGlobalContext.addESMPlugin(Json(ObjectMap.of<String,String>(
            "name", "Test_2",
            "author", "",
            "description", "",
            "main", "main.mjs",
            "version", ""
        )), data1)
        scriptPluginGlobalContext.loadESMPlugins().eachAll {
            it.main.onEnable()
            it.main.init()
        }


//        val dataOnlyOneFile = OrderedMap<String,ByteArray>().apply {
//            put("/main.js", ExtractUtil.bytes("""
//                function main() {
//                    importClass("net.rwhps.server.plugin.Plugin")
//
//                    var plugin = new JavaAdapter(Plugin ,{
//                        onEnable: function() {
//                            print("Hi onEnable")
//                        },
//                        init: function() {
//                            print("oneClass")
//                        }
//                    })
//
//                    return plugin
//                }
//            """.trimIndent(), Data.UTF_8))
//        }
//        // 这个是单文件, 不可以使用import/export
//        val oneClassOnlyOneFile = JavaScriptPlugin.loadJavaScriptPlugin(dataOnlyOneFile, "/main.js", ExtractUtil.str(dataOnlyOneFile["/main.js"]!!, Data.UTF_8))
//
//        oneClassOnlyOneFile.onEnable()
//        oneClassOnlyOneFile.init()
    }
}