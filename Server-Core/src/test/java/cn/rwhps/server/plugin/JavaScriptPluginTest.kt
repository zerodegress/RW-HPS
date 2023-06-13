package cn.rwhps.server.plugin

import net.rwhps.server.plugin.JavaScriptPlugin
import org.junit.jupiter.api.Test

internal class JavaScriptPluginTest() {
    @Test
    fun loadPlugin() {
        JavaScriptPlugin.loadJavaScriptPlugin("var a=5;function main(){return new JavaAdapter(Packages.net.rwhps.server.plugin.Plugin,{})}")
    }
}
