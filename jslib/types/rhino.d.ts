/** java.lang */
declare namespace java.lang {
    class String {
        constructor();
    }
}

declare namespace Packages.net.rwhps.server.plugin {
    class Plugin {
        constructor(obj: {
            onEnable: () => void,
            registerCoreCommands?: (handler: Packages.net.rwhps.server.util.game.CommandHandler) => void 
        });
    }
}

declare namespace Packages.net.rwhps.server.util.game {
    class CommandHandler {
        constructor(prefix: java.lang.String);
        setPrefix(prefix: java.lang.String): void;
        handleMessage(message: java.lang.String);
    }

    namespace CommandHandler {
        enum ResponseType {
            noCommand, unknownCommand, fewArguments, manyArguments, valid
        }
        
        class CommandResponse {
            
        }
    }
}