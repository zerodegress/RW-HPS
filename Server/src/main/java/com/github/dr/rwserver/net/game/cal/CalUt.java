package com.github.dr.rwserver.net.game.cal;

import com.github.dr.rwserver.net.game.KongZhi;
import com.github.dr.rwserver.util.log.Log;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import kotlin.text.Charsets;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class CalUt {
    public static List<ChannelInfo> twoOver=new LinkedList<>();
    public static Map<ChannelId, ChannelInfo> channelInfos=new HashMap<>(120);
    public static Map<ChannelId, ChannelInfo> channelDone=new HashMap<>(200);
    static File out=new File("con"+new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒").format(System.currentTimeMillis()) +".csv");
    static {
        if(!out.exists()){
            try {
                out.createNewFile();
                FileOutputStream fos = new FileOutputStream(out);
                fos.write("序号,ip,端口,玩家名称,连接时间,断开时间".getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void throwGab(){
        Iterator<ChannelInfo> channelInfoIterator=twoOver.iterator();
        int i=0;
        while (channelInfoIterator.hasNext()){
            ChannelInfo c=channelInfoIterator.next();
            if(!channelInfos.containsKey(c.getCh().id())&&!KongZhi.hasChannel(c.getCh())){
                channelInfoIterator.remove();
                Channel ch = c.getCh();
                try{
                    ch.close();
                    i++;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
//            }
        }
        Log.clog("清理连接："+i+"个");
    }
    public static synchronized void leave(ChannelId id){
        ChannelInfo channelInfo = channelInfos.remove(id);
        if(channelInfo==null) return;
        channelInfo.setEndTime(System.currentTimeMillis());
        channelDone.put(id,channelInfo);
        if(channelDone.size()>199){
            append(channelDone.values());
            channelDone.clear();
        }
    }
    public static void flushAll(){
        ArrayList<ChannelInfo> all = new ArrayList<>();
        all.addAll(channelInfos.values());
        all.addAll(channelDone.values());
        append(all);
    }
    private static void append(Collection<ChannelInfo> channelInfos){
        StringBuilder sb = new StringBuilder("\n");
        for(ChannelInfo ci:channelInfos){
            if(ci==null) continue;
            //seq,ip,port,playerName,start,end
            InetSocketAddress address = (InetSocketAddress) ci.getCh().remoteAddress();
            sb.append(ci.getSeq()).append(",")
                    .append(address.getAddress().getHostAddress()).append(",")
                    .append(address.getPort()).append(",")
                    .append(ci.getP()==null?"":ci.getP().name.replace(",","，")).append(",")
                    .append(ci.getStartTime()).append(",")
                    .append(ci.getEndTime()).append("\n");
        }
        BufferedWriter bw=null;
        try(FileOutputStream fos = new FileOutputStream(out, true); ){
            bw = new BufferedWriter(new OutputStreamWriter(fos,StandardCharsets.UTF_8));
            bw.append(sb.toString());
            bw.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
