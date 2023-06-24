package net.rwhps.server.plugin.beta.httpapi

import net.rwhps.server.util.file.FileUtils
import net.rwhps.server.util.inline.toGson
import net.rwhps.server.util.inline.toPrettyPrintingJson

object ConfigHelper {
    var config: Config = Config()

    fun init(configFile: FileUtils) {
        if (configFile.notExists()) {
            configFile.createNewFile()
            configFile.writeFile(config.toPrettyPrintingJson())
            println(configFile.path)
        } else {
            config = Config::class.java.toGson(configFile.readFileStringData())
        }
    }
}