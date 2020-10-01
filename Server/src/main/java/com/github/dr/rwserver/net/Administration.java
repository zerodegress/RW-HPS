package com.github.dr.rwserver.net;

import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.data.global.Settings;
import com.github.dr.rwserver.struct.ObjectMap;
import com.github.dr.rwserver.struct.Seq;

import static com.github.dr.rwserver.util.Convert.castSeq;

/**
 * @author Dr
 */
public class Administration {

    private Seq<ChatFilter> chatFilters = new Seq<>();
    private NetConnectProtocolData netConnectProtocolData = null;
    public final Seq<String> bannedIPs;
    public final Seq<String> bannedUUIDs;
    public final Seq<String> whitelist;
    public final Seq<String> playerData;
    public final ObjectMap<String,PlayerInfo> playerDataCache = new ObjectMap<>();

    public Administration(Settings settings){
        addChatFilter((player, message) -> {
            return message;
        });
        bannedIPs = castSeq(settings.getObject("bannedIPs",Seq.class,new Seq()),String.class);
        bannedUUIDs = castSeq(settings.getObject("bannedUUIDs",Seq.class,new Seq()),String.class);
        whitelist = castSeq(settings.getObject("whitelist",Seq.class,new Seq()),String.class);
        playerData = castSeq(settings.getObject("playerData",Seq.class,new Seq()),String.class);
    }

    public void save(Settings settings) {
        settings.putObject("bannedIPs",bannedIPs);
        settings.putObject("bannedUUIDs",bannedUUIDs);
        settings.putObject("whitelist",whitelist);
        settings.putObject("playerData",playerData);
    }

    /**
     * 添加聊天过滤器。这将改变每个玩家的聊天消息
     * 此功能可用于实现宣誓过滤器和特殊命令之类的功能
     * 请注意，未过滤命令
     */
    public void addChatFilter(ChatFilter filter){
        chatFilters.add(filter);
    }

    /** 过滤掉聊天消息 */
    public String filterMessage(Player player, String message){
        String current = message;
        for(ChatFilter f : chatFilters){
            current = f.filter(player, message);
            if(current == null) {
                return null;
            }
        }
        return current;
    }

    public void addAdmin(String uuid) {
        playerData.add(uuid);
    }

    public void removeAdmin(String uuid) {
        playerData.remove(uuid);
    }

    public boolean isAdmin(String uuid) {
        return playerData.contains(uuid);
    }

    public void setNetConnectProtocol(NetConnectProtocolData protocolData){
        netConnectProtocolData = protocolData;
    }

    public NetConnectProtocolData getNetConnectProtocol(){
        return netConnectProtocolData;
    }

    public interface ChatFilter{
        /**
         * 过滤消息
         * @param player Player
         * @param message Message
         * @return 过滤后的消息 空字符串表示不应发送该消息
         */
        String filter(Player player, String message);
    }

    public static class PlayerInfo {
        public final String uuid;
        public long timesKicked = 0;
        public long timesJoined = 0;
        public long timeMute = 0;
        public boolean admin = false;

        public PlayerInfo(String uuid){
            this.uuid = uuid;
        }

        public PlayerInfo(String uuid,boolean admin){
            this.uuid = uuid;
            this.admin = admin;
        }

        public PlayerInfo(String uuid,long timesKicked,long timeMute){
            this.uuid = uuid;
            this.timesKicked = timesKicked;
            this.timeMute = timeMute;
        }

        public PlayerInfo(String uuid,long timesKicked,long timeMute,boolean admin){
            this.uuid = uuid;
            this.admin = admin;
            this.timesKicked = timesKicked;
            this.timeMute = timeMute;
        }
    }

    public static class NetConnectProtocolData {
        public final AbstractNetConnect protocol;
        public final int version;

        public NetConnectProtocolData(AbstractNetConnect protocol,int version) {
            this.protocol = protocol;
            this.version = version;
        }
    }
}
