/** java.lang */
declare namespace java.lang {
    class String {
        constructor(str: string);
    }

    namespace reflect {
        namespace Array {
            function newInstance(type: unknown, length: number): unknown;
        }
    }
}

declare namespace Packages {
    namespace net.rwhps.server {
        namespace plugin {
            class Plugin {
                constructor(obj: {
                    onEnable: () => void,
                    registerCoreCommands?: (handler: Packages.net.rwhps.server.util.game.CommandHandler) => void 
                });
                //TODO
            }
        }

        namespace util.game {
            class CommandHandler {
                constructor(prefix: string);
                setPrefix(prefix: string): void;
                handleMessage(message: string);
                //TODO
            }
        
            namespace CommandHandler {
                enum ResponseType {
                    noCommand, unknownCommand, fewArguments, manyArguments, valid
                }
        
                class CommandParam {
                    readonly name: string;
                    readonly optional: boolean;
                    readonly variadic: boolean;
                    
                    constructor(name: string, optional: boolean, variadic: boolean);
                }
        
                interface CommandRunner<T> {
                    accept(args: any, parameter: T): void;
                }
        
                class Command {
                    readonly text: string;
                    readonly paramText: string;
                    readonly description: string;
        
                    readonly params: CommandParam[];
        
                    constructor(text: string, paramText: string, description: string, runner: CommandRunner<unknown>);
                }
        
                class CommandResponse {
                    //TODO
                }
            }
        }
        namespace struct {
            class ObjectMap<K, V> {
                
            }
        }
    }
}