package com.github.dr.rwserver.game;

import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.net.core.AbstractNetConnect;

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
    public static class PlayerJoinEvent {
        public final Player player;

        public PlayerJoinEvent(Player player) {
            this.player = player;
        }
    }

    /** 玩家重连 */
    public static class PlayerReJoinEvent {
        public final Player player;

        public PlayerReJoinEvent(Player player) {
            this.player = player;
        }
    }

    /** 玩家连接密码验证.*/
    public static class PlayerConnectPasswdCheckEvent {
        /** 游戏实现协议 */
        public final AbstractNetConnect abstractNetConnect;
        /** 密码SHA256的16进 */
        public final String passwd;
        /** 密码SHA256的16进 */
        public boolean result = false;
        /** 你可以给他设置一个名字 */
        public String name = "";

        public PlayerConnectPasswdCheckEvent(AbstractNetConnect abstractNetConnect, String passwd) {
            this.abstractNetConnect = abstractNetConnect;
            this.passwd = passwd;
        }
    }

    /** 玩家连接时.*/
    public static class PlayerConnectEvent {
        public final Player player;

        public PlayerConnectEvent(Player player) {
            this.player = player;
        }
    }

    /** 玩家离开时 */
    public static class PlayerLeaveEvent {
        public final Player player;

        public PlayerLeaveEvent(Player player) {
            this.player = player;
        }
    }

    /** 玩家发言时 */
    public static class PlayerChatEvent {
        public final Player player;
        public final String message;

        public PlayerChatEvent(Player player,String message) {
            this.player = player;
            this.message = message;
        }
    }

    /** 开始游戏 */
    public static class GameStartEvent {
    }

    /** 结束游戏 */
    public static class GameOverEvent {
    }

    /** 玩家被ban */
    public static class PlayerBanEvent {
        public final Player player;

        public PlayerBanEvent(Player player) {
            this.player = player;
        }
    }

    /** 玩家被解除ban */
    public static class PlayerUnbanEvent {
        public final Player player;

        public PlayerUnbanEvent(Player player) {
            this.player = player;
        }
    }

    /** 玩家被banIp */
    public static class PlayerIpBanEvent {
        public final Player player;

        public PlayerIpBanEvent(Player player) {
            this.player = player;
        }
    }

    /** 玩家被解banIp */
    public static class PlayerIpUnbanEvent {
        public final String ip;

        public PlayerIpUnbanEvent(String ip) {
            this.ip = ip;
        }
    }
}