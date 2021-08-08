package com.github.dr.rwserver.command;

import com.github.dr.rwserver.net.core.server.AbstractNetConnect;
import com.github.dr.rwserver.util.game.CommandHandler;

public class BotCommands {
    public BotCommands(CommandHandler handler) {
        handler.<AbstractNetConnect>register("reloadmaps", "", (arg, con) -> {

        });
    }
}
