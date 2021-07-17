package com.github.dr.rwserver.data.plugin;

import com.github.dr.rwserver.game.EventType.*;
import com.github.dr.rwserver.game.EventType.PlayerReJoinEvent;
import com.github.dr.rwserver.game.EventType.ServerLoadEvent;
import com.github.dr.rwserver.plugin.event.AbstractEvent;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.game.Events;
import com.github.dr.rwserver.util.threads.GetNewThredPool;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadPoolExecutor;

class PluginEventManage {
    private static final Seq<AbstractEvent> pluginEventData = new Seq<>(2);
    private static final ThreadPoolExecutor executorService = GetNewThredPool.getNewFixedThreadPool(5,"PluginEventASync-");

    protected PluginEventManage() {
        registerEventAll();
    }
    protected static void add(@NotNull final AbstractEvent abstractEvent) {
        pluginEventData.add(abstractEvent);
    }

    private static void registerEventAll() {
        /* ASync */
        Events.on(ServerLoadEvent.class, e -> executorService.execute(() ->pluginEventData.each(AbstractEvent::registerServerLoadEvent)));

        Events.on(PlayerJoinEvent.class, e -> pluginEventData.each(p->p.registerPlayerJoinEvent(e.player)));
        Events.on(PlayerReJoinEvent.class, e -> pluginEventData.each(p->p.registerPlayerReJoinEvent(e.player)));

        Events.on(PlayerConnectPasswdCheckEvent.class, e -> pluginEventData.each(p-> {
            String[] strings = p.registerPlayerConnectPasswdCheckEvent(e.abstractNetConnect, e.passwd);
            e.result = Boolean.parseBoolean(strings[0]);
            e.name = strings[1];
        }));

        /* ASync */
        Events.on(PlayerConnectEvent.class, e -> executorService.execute(() ->pluginEventData.each(p->p.registerPlayerConnectEvent(e.player))));
        /* ASync */
        Events.on(PlayerLeaveEvent.class, e -> executorService.execute(() ->pluginEventData.each(p->p.registerPlayerLeaveEvent(e.player))));

        /* ASync */
        Events.on(PlayerChatEvent.class, e -> executorService.execute(() ->pluginEventData.each(p->p.registerPlayerChatEvent(e.player,e.message))));

        /* ASync */
        Events.on(GameStartEvent.class, e -> executorService.execute(() ->pluginEventData.each(AbstractEvent::registerGameStartEvent)));
        /* ASync */
        Events.on(GameOverEvent.class, e -> executorService.execute(() ->pluginEventData.each(AbstractEvent::registerGameOverEvent)));

        /* ASync */
        Events.on(PlayerBanEvent.class, e -> executorService.execute(() ->pluginEventData.each(p->p.registerPlayerBanEvent(e.player))));
        /* ASync */
        Events.on(PlayerUnbanEvent.class, e -> executorService.execute(() ->pluginEventData.each(p->p.registerPlayerUnbanEvent(e.player))));
        /* ASync */
        Events.on(PlayerIpBanEvent.class, e -> executorService.execute(() ->pluginEventData.each(p->p.registerPlayerIpBanEvent(e.player))));
        /* ASync */
        Events.on(PlayerIpUnbanEvent.class, e -> executorService.execute(() ->pluginEventData.each(p->p.registerPlayerIpUnbanEvent(e.ip))));
    }
}
