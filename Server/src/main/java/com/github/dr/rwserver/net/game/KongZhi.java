package com.github.dr.rwserver.net.game;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.func.StrCons;
import com.github.dr.rwserver.ga.GroupGame;
import com.github.dr.rwserver.struct.OrderedMap;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.game.CommandHandler;
import com.github.dr.rwserver.util.log.Log;
import com.google.gson.Gson;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ChannelHandler.Sharable
public class KongZhi extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static Set<Channel> connected=new HashSet<>(4);
    private static final String pwd=Data.config.readString("wsPassword", "flzx3qc");
    private static final Pattern msgPattern =  Pattern.compile("-p(.*?) -(.*?) (.*)");
    private static final int CTL=1;
    private static final int CONFIG=2;
    private static final int PING=3;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
//        Log.clog("收到ws："+msg);
        Matcher matcher = msgPattern.matcher(msg.text());
        if(matcher.find()){
            if(!connected.contains(ctx.channel())){
                if(!pwd.equals(matcher.group(1))) {
                    ctx.writeAndFlush(error(100));
                    ctx.channel().close();
                    return;
                }else connected.add(ctx.channel());
            }

            int op=-1;
            try{
                op=Integer.parseInt(matcher.group(2));
            }catch (NumberFormatException ne){
                ctx.writeAndFlush(error(98));
            }
            String tar=matcher.group(3);
            if(op==CTL){
                handleCmd(ctx,tar);
            }
        }else ctx.writeAndFlush(error(99));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        connected.remove(ctx.channel());
        super.channelInactive(ctx);
    }

    private TextWebSocketFrame error(int code){
        return new TextWebSocketFrame("错误："+code);
    }

    private void sendCmd(ChannelHandlerContext conn){
        Seq<CommandHandler.Command> commandList = Data.SERVERCOMMAND.getCommandList();
        ArrayList<Map<String,String>> cmdList = new ArrayList<>();
        commandList.forEach(x->{
            HashMap<String, String> cmd = new HashMap<>();
            cmd.put("h",x.text);
            cmd.put("p",x.paramText);
            String des=x.description;
            if (x.description.startsWith("#")) {
                des=des.substring(1);
            } else {
                des=Data.localeUtil.getinput(des);
            }
            cmd.put("d",des);
            cmdList.add(cmd);
        });
        conn.writeAndFlush(new TextWebSocketFrame("-c "+ JSONObject.toJSONString(cmdList)));
    }
    @SuppressWarnings("unchecked")
    private void handleCmd(ChannelHandlerContext conn,String message){
        if(message.startsWith("-rc")) sendCmd(conn);
        else if(message.equals("-ts")) {
            conn.writeAndFlush(new TextWebSocketFrame(gameStateInfo()));
        }else if(message.equals("-gf")){
            conn.writeAndFlush(new TextWebSocketFrame("-gf "+getServerConfigString()));
        }else if(message.startsWith("-sf")){
            Map<String,String> map = new Gson().fromJson(message.replace("-sf ", ""), Map.class);
            map.forEach((k,v)->Data.config.getData().put(k,v));
        }
        else {
            Log.clog("接收到游戏板消息："+message);
            Data.SERVERCOMMAND.handleMessage(message,(StrCons) Log::clog);
        }
    }
    private String getServerConfigString(){
        OrderedMap<String, String> data = Data.config.getData();
        final Map<String,String> map = new HashMap<>();
        data.each(map::put);
        return JSONObject.toJSONString(map, SerializerFeature.PrettyFormat);
    }
    private static ArrayList<String> fieldName = new ArrayList<>();
    static {
        fieldName.add("玩家昵称");
        fieldName.add("uuid");
        fieldName.add("位置");
        fieldName.add("延迟");
        fieldName.add("队伍");
        fieldName.add("管理");
        fieldName.add("游戏状态");
        fieldName.add("组");
        fieldName.add("平均延迟");
        fieldName.add("ping/次");
        fieldName.add("连接地址");
    }
    @SuppressWarnings("unchecked")
    public static String gameStateInfo(){
        List<Object> info=new ArrayList<>();
        info.add(fieldName);
        Data.playerAll.forEach(g->{
            Optional<Object> group = info.stream().filter(o -> o instanceof Map && ((List) ((Map) o).get("head")).get(0).equals("组" + g.groupId)).findAny();
            List<List> players;
            if(group.isPresent()){
                 players = (List<List>) ((Map) group.get()).get("body");
            }else {
                HashMap<String,Object> data = new HashMap<>();
                List<String> prop=new ArrayList<>();
                prop.add("组" +g.groupId);
                prop.add(GroupGame.games.get(g.groupId).isStartGame?"游戏中":"战役室");
                prop.add(GroupGame.gU(g.groupId).startTime+"");
                players= new ArrayList<>();
                data.put("head",prop);
                data.put("body",players);
                info.add(data);
            }
            ArrayList<String> player = new ArrayList<>();
            player.add(g.name);
            player.add(g.uuid);
            player.add(g.site+"");
            player.add(g.ping+"");
            player.add(g.team+"");
            player.add(g.isAdmin+"");
            player.add(Data.playerGroup.contains(g)? g.dead?"已被击败":"正常":"断开");
            player.add(g.groupId+"");
            player.add(g.avgPing+"");
            player.add(g.pingTimes+"");
            player.add(g.con.getIp()+":"+ g.con.getPort());
            players.add(player);
        });
        return "-ts"+JSONObject.toJSONString(info);
    }

    public static void broadCast(String msg){
        connected.forEach(x->x.writeAndFlush(new TextWebSocketFrame(msg)));
    }
}
