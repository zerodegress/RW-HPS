package net.rwhps.server.plugin

import org.junit.jupiter.api.Test

class JavaScriptPluginTest {

    @Test
    fun loadJavaScriptPlugin() {
        JavaScriptPlugin.loadJavaScriptPlugin("""
            function main() {
            const Plugin = Java.type("net.rwhps.server.plugin.Plugin")
        
            const plugin = Java.extend(Plugin ,{
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