package cn.rwhps.server.plugin.beta.httpapi

import cn.rwhps.server.util.file.FileUtil
import cn.rwhps.server.util.inline.toGson
import cn.rwhps.server.util.inline.toPrettyPrintingJson

object ConfigHelper {
    var config: Config = Config()

    fun init(configFile: FileUtil) {
        if (configFile.notExists()) {
            configFile.createNewFile()
            configFile.writeFile(config.toPrettyPrintingJson())
            println(configFile.path)
        } else {
            config = Config::class.java.toGson(configFile.readFileStringData())
        }
    }
}