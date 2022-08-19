package cn.rwhps.server.plugin.beta.httpapi

import cn.rwhps.server.util.file.FileUtil
import com.google.gson.Gson

object ConfigHelper {
    private val gson = Gson()
    var config: Config = Config()

    fun init(configFile: FileUtil) {
        if (configFile.notExists()) {
            configFile.createNewFile()
            configFile.writeFile(gson.toJson(config))
            println(configFile.path)
        } else {
            config = gson.fromJson(configFile.readFileStringData(), Config::class.java)
        }
    }
}