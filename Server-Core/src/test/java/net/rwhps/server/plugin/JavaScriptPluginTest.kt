package net.rwhps.server.plugin

import net.rwhps.server.data.bean.BeanPluginInfo
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

        val plugin1 = OrderedMap<String, ByteArray>().apply {
            put("/\$plugins/a/index.js", ExtractUtils.bytes("""
                export const a = 10
                export default new (Java.extend(Plugin, {}))()
            """.trimIndent()))
        }

        val plugin2 = OrderedMap<String, ByteArray>().apply {
            put("/\$plugins/b/index.js", ExtractUtils.bytes("""
                import { a } from "a"
                export default new (Java.extend(Plugin, {
                    onEnable() {
                        console.log(a)
                    }
                }))()
            """.trimIndent()))
        }

        // 这个是模块化, 可以使用import/export
        scriptPluginGlobalContext.addESMPlugin(
            BeanPluginInfo(
                name = "a",
                author = "zerodegress",
                main = "index.js"
            ), plugin1)
        scriptPluginGlobalContext.addESMPlugin(
            BeanPluginInfo(
                name = "b",
                author = "zerodegress",
                main = "index.js"
            ), plugin2)
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