package com.github.dr.rwserver.game;

import com.github.dr.rwserver.data.Player;

/**
 * @author Dr
 */
public class EventType {
    /** 服务器初始化 */
    public static class ServerLoadEvent {

        public ServerLoadEvent() {
        }
    }
	/** 玩家加入 */
    public static class PlayerJoin {
        public final Player player;

        public PlayerJoin(Player player) {
            this.player = player;
        }
    }

    /** 玩家重连 */
    public static class PlayerReJoin {
        public final Player player;

        public PlayerReJoin(Player player) {
            this.player = player;
        }
    }

    /** 玩家连接时.*/
    public static class PlayerConnect {
        public final Player player;

        public PlayerConnect(Player player) {
            this.player = player;
        }
    }

    /** 玩家离开时 */
    public static class PlayerLeave {
        public final Player player;

        public PlayerLeave(Player player) {
            this.player = player;
        }
    }

    public static class PlayerChatEvent {
        public final Player player;
        public final String message;

        public PlayerChatEvent(Player player,String message) {
            this.player = player;
            this.message = message;
        }
    }

    public static class GameStartEvent {
    }

    public static class GameOverEvent {
    }
    
    public static class PlayerBanEvent {
        public final Player player;

        public PlayerBanEvent(Player player) {
            this.player = player;
        }
    }
    
    public static class PlayerUnbanEvent {
        public final Player player;

        public PlayerUnbanEvent(Player player) {
            this.player = player;
        }
    }
    
    public static class PlayerIpBanEvent {
        public final Player player;


        public PlayerIpBanEvent(Player player) {
            this.player = player;
        }
    }
    
    public static class PlayerIpUnbanEvent {
        public final String ip;


        public PlayerIpUnbanEvent(String ip) {
            this.ip = ip;
        }
    }
}