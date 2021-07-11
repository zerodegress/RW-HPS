package com.github.dr.rwserver.plugin.event;

import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.net.core.AbstractNetConnect;

public interface AbstractEvent {
    /** 服务器初始化 [异步-ASync] */
    default void registerServerLoadEvent() {
    }

    /**
     * 玩家加入 [同步-Synchronization]
     * @param player Player
     */
    default void registerPlayerJoinEvent(Player player) {
    }

    /**
     * 玩家重连 [同步-Synchronization]
     * @param player Player
     */
    default void registerPlayerReJoinEvent(Player player) {
    }

    /**
     * 玩家连接密码验证 [同步-Synchronization]
     * @param abstractNetConnect 游戏实现协议
     * @param passwd 密码SHA256的16进
     * @return String[0]=密码是否正确(Boolean) String[1]=你可以给他设置一个名字
     */
    default String[] registerPlayerConnectPasswdCheckEvent(AbstractNetConnect abstractNetConnect, String passwd) {
        return new String[]{"false",""};
    }

    /**
     * 玩家连接时 [异步-ASync]
     * @param player Player
     */
    default void registerPlayerConnectEvent(Player player) {
    }

    /**
     * 玩家离开时 [异步-ASync]
     * @param player Player
     */
    default void registerPlayerLeaveEvent(Player player) {
    }

    /**
     * 玩家发言时 [异步-ASync]
     * @param player
     * @param message
     */
    default void registerPlayerChatEvent(Player player,String message) {
    }

    /** 开始游戏 [异步-ASync] */
    default void registerGameStartEvent() {
    }

    /** 结束游戏 [异步-ASync] */
    default void registerGameOverEvent() {
    }

    /** 玩家被ban [异步-ASync] */
    default void registerPlayerBanEvent(Player player) {
    }

    /** 玩家被解除ban [异步-ASync] */
    default void registerPlayerUnbanEvent(Player player) {
    }

    /** 玩家被banIp [异步-ASync] */
    default void registerPlayerIpBanEvent(Player player) {
    }

    /** 玩家被解banIp [异步-ASync] */
    default void registerPlayerIpUnbanEvent(String ip) {
    }
}
