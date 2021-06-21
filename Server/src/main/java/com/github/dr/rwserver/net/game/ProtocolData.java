package com.github.dr.rwserver.net.game;

import com.github.dr.rwserver.net.core.AbstractNetConnect;
import com.github.dr.rwserver.net.core.AbstractNetPacket;
import com.github.dr.rwserver.net.core.TypeConnect;

public class ProtocolData {
    private int GameNetVersion;
    protected AbstractNetConnect abstractNetConnect;
    protected TypeConnect typeConnect;
    public String AbstractNetConnectVersion;
    public String AbstractNetPacketVersion;
    public String TypeConnectVersion;
    public AbstractNetPacket abstractNetPacket;

    public void setNetConnectProtocol(AbstractNetConnect protocolData,int gameNetVersion){
        this.abstractNetConnect = protocolData;
        this.AbstractNetConnectVersion = protocolData.getVersion();
        this.GameNetVersion = gameNetVersion;
    }

    public void setNetConnectPacket(AbstractNetPacket packet,String version){
        this.abstractNetPacket = packet;
        this.AbstractNetPacketVersion = version;
    }

    public void setTypeConnect(TypeConnect typeConnect){
        this.typeConnect = typeConnect;
        this.TypeConnectVersion = typeConnect.getVersion();
    }

    protected void update(AbstractNetConnect abstractNetConnect,TypeConnect typeConnect) {
        this.abstractNetConnect = abstractNetConnect;
        this.typeConnect = typeConnect;
    }

    public int getGameNetVersion() {
        return GameNetVersion;
    }
}
