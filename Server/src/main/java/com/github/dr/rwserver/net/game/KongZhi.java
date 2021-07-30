package com.github.dr.rwserver.net.game;

import com.alibaba.fastjson.JSONObject;
import com.github.dr.rwserver.command.GameTimeLapse;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.func.StrCons;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.game.CommandHandler;
import com.github.dr.rwserver.util.log.Log;
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
    private void handleCmd(ChannelHandlerContext conn,String message){
        if(message.startsWith("-rc")) sendCmd(conn);
        else if(message.equals("-ts")) {
            conn.writeAndFlush(new TextWebSocketFrame(gameStateInfo()));
        }else {
            Log.clog("接收到游戏板消息："+message);
            Data.SERVERCOMMAND.handleMessage(message,(StrCons) Log::clog);
        }
    }
    public static String gameStateInfo(){
        ArrayList<List<String>> players = new ArrayList<>();
        ArrayList<String> fieldName = new ArrayList<>();
        fieldName.add("玩家昵称");
        fieldName.add("uuid");
        fieldName.add("位置");
        fieldName.add("延迟");
        fieldName.add("队伍");
        fieldName.add("管理");
        fieldName.add("游戏状态");
        fieldName.add("连接地址");
        players.add(fieldName);
        Data.playerGroup.forEach(p->{
            ArrayList<String> player = new ArrayList<>();
            player.add(p.name);
            player.add(p.uuid);
            player.add(p.site+"");
            player.add(p.ping+"");
            player.add(p.team+"");
            player.add(p.isAdmin+"");
            player.add(Data.game.isStartGame?p.dead?"已被击败":"比赛中":"战役室");
            player.add(p.con.getIp()+":"+p.con.getPort());
            players.add(player);
        });
        HashMap<String,Object> data = new HashMap<>();
        data.put("sTime", GameTimeLapse.getStartTime()+"");
        data.put("players",players);
        data.put("isGameStart",Data.game.isStartGame);
        return "-ts"+JSONObject.toJSONString(data);
    }
    public static void broadCast(String msg){
        connected.forEach(x->x.writeAndFlush(new TextWebSocketFrame(msg)));
    }
}
