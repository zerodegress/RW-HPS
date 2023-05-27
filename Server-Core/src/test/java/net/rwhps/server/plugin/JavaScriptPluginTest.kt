package net.rwhps.server.plugin

import org.junit.jupiter.api.Test

class JavaScriptPluginTest {

    @Test
    fun loadJavaScriptPlugin() {
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