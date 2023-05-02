package net.rwhps.server.plugin

import net.rwhps.server.data.global.Data
import net.rwhps.server.func.StrCons
import net.rwhps.server.util.file.FileUtil
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class JavaScriptPluginTest {

    @Test
    fun loadJavaScriptPlugin() {
        /** (Test 环境不兼容 readJar)
        val packet = JavaScriptPlugin.loadJavaScriptPlugin("""
            function main() {
            importPackage("net.rwhps.server.plugin")

            var plugin = new JavaAdapter(Plugin ,{
                onEnable: function() {
                    print("Hi")
                },
                init: function() {
                    print("packet")
                }
            })

            return plugin
        }
        """.trimIndent())
        */
        val oneClass = JavaScriptPlugin.loadJavaScriptPlugin("""
            function main() {
            importClass("net.rwhps.server.plugin.Plugin")
        
            var plugin = new JavaAdapter(Plugin ,{
                onEnable: function() {
                    print("Hi")
                },
                init: function() {
                    print("oneClass")
                }
            })
            
            return plugin
        }
        """.trimIndent())
    }
}