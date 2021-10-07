package com.github.dr.rwserver.net.game.cal;


import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.util.log.Log;
import io.netty.channel.Channel;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.concurrent.atomic.AtomicLong;

public class ChannelInfo {
    private long seq;
    private Channel ch;
    private long startTime;
    private long endTime;
    private Player p;
    private static SimpleDateFormat formatter=new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
    private static AtomicLong idx=new AtomicLong(0);
    public void Evaluate(String name){
        try {
            Method method = ch.getClass().getMethod(name);
            Log.clog(method.invoke(ch).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public ChannelInfo(Channel ch, long startTime) {
        this.ch = ch;
        this.startTime = startTime;
        seq=idx.getAndIncrement();
    }
    @Override
    public String toString() {
        return "ChannelInfos{" +
                "ch=" + ch +
                ", startTime=" +formatter.format(startTime)+
                ", p=" + p +
                ", isOpen=" + ch.isOpen() +
                '}';
    }

    public Channel getCh() {
        return ch;
    }

    public void setCh(Channel ch) {
        this.ch = ch;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public Player getP() {
        return p;
    }

    public void setP(Player p) {
        this.p = p;
    }

    public long getEndTime() {
        return endTime;
    }
    public long getSeq(){return seq;}
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
