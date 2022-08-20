package cn.rwhps.server.plugin.beta.httpapi.handlers.post

import cn.rwhps.server.data.global.Data
import cn.rwhps.server.func.StrCons
import cn.rwhps.server.net.http.SendWeb
import cn.rwhps.server.plugin.beta.httpapi.handlers.BasePostHandler
import cn.rwhps.server.plugin.beta.httpapi.responses.BaseResp
import cn.rwhps.server.util.game.CommandHandler
import cn.rwhps.server.util.inline.toPrettyPrintingJson
import java.net.URLDecoder

class CommandGetHandler : BasePostHandler() {
    override fun post(postUrl: String, urlData: String, data: String, send: SendWeb) {
        super.post(postUrl, urlData, data, send)
        val command = URLDecoder.decode(param.getData("exec"), "UTF-8")
        if (command.isEmpty()) send(BaseResp(data = "参数错误").toPrettyPrintingJson())
        var text = ""
        val response = Data.SERVER_COMMAND.handleMessage(command,
            StrCons { obj: String -> text += "$obj${Data.LINE_SEPARATOR}" })
        if (response != null && response.type != CommandHandler.ResponseType.noCommand) {
            if (response.type != CommandHandler.ResponseType.valid) {
                text = when (response.type) {
                    CommandHandler.ResponseType.manyArguments -> {
                        "Too many arguments. Usage: " + response.command.text + " " + response.command.paramText
                    }
                    CommandHandler.ResponseType.fewArguments -> {
                        "Too few arguments. Usage: " + response.command.text + " " + response.command.paramText
                    }
                    else -> {
                        "Unknown command. Check help"
                    }
                }
            }
        }
        if (text.isNotEmpty()) {
            send(BaseResp(data = text).toPrettyPrintingJson())
        }
    }
}