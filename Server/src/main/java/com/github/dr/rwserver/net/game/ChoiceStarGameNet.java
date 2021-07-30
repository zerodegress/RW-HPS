package com.github.dr.rwserver.net.game;

import com.github.dr.rwserver.util.log.Log;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;

public class ChoiceStarGameNet extends StartGameNetTcp{
    protected ChoiceStarGameNet(StartNet startNet) {
        super(startNet);
    }
    private final KongZhi wsHandler=new KongZhi();
    private static Map<String,byte[]> res=new HashMap<>();
    private final static String WS_URI="/ws";
    private SocketChannel socketChannel;
    static {
        InputStream a=null;
        JarFile jarFile=null;
        try {
            URL res1 = ChoiceStarGameNet.class.getResource("res");
            String prefix = res1.getFile().split("!/")[1];
            jarFile =  ((JarURLConnection) res1.openConnection()).getJarFile();
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()){
                JarEntry jarEntry = entries.nextElement();
                String name = jarEntry.getName();
                if (jarEntry.isDirectory() ||name.endsWith(".class")||!name.startsWith(prefix)) {
                    continue;
                }
                URL resource = ChoiceStarGameNet.class.getClassLoader().getResource(name);
                a=resource.openStream();
                res.put(name.replace(prefix,""), a.readAllBytes());
            }
            if(res.containsKey("/index.html")) res.put("/",res.get("/index.html"));
        } catch (Exception e) {
            e.printStackTrace();
            Log.clog("无法加载静态页面资源");
        }finally {
            if(null!=jarFile){
                try {
                    if(null!=a)try{
                        a.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    jarFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
//        ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
        this.socketChannel=ch;
        ch.pipeline().addLast(new Divider());
    }
    void superInitChannel() throws Exception {
        super.initChannel(socketChannel);
    };

    class Divider extends ChannelInboundHandlerAdapter {
        private boolean isHttpReq(String head){
            return (
                    head.startsWith("GET ")||
                            head.startsWith("POST ")||
                            head.startsWith("DELETE ")||
                            head.startsWith("HEAD ")||
                            head.startsWith("PUT ")
            );
        }
        @Deprecated
        private byte[] loadRes(String name) throws IOException {
            InputStream resourceAsStream = ChoiceStarGameNet.class.getResourceAsStream("res" + name);
            if(null==resourceAsStream) return "资源不存在".getBytes(StandardCharsets.UTF_8);
            try {
                return resourceAsStream.readAllBytes();
            }finally {
                resourceAsStream.close();
            }
        }
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf firstData = (ByteBuf) msg;
            String headS = firstData.toString(StandardCharsets.UTF_8);
            if(isHttpReq(headS)){
                if(headS.startsWith("GET "+WS_URI)){
                    ctx.pipeline().addLast(
                            new LoggingHandler(LogLevel.INFO),
                            new IdleStateHandler(10,0,0),
                            new ChannelDuplexHandler(){
                            @Override
                            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                IdleStateEvent evt1 = (IdleStateEvent) evt;
                                    if(evt1.state()== IdleState.READER_IDLE){
                                        Log.clog("已经10秒没有读到数据了,主动断开连接"+ctx.channel());
                                        ctx.channel().close();
                                    }
                                }
                            },
                            new HttpServerCodec(),
                            new ChunkedWriteHandler(),
                            new HttpObjectAggregator(1048576),
                            new WebSocketServerProtocolHandler(WS_URI)
                    );
                    ctx.channel().pipeline().addLast(wsHandler);
                }else {
                    ctx.channel().pipeline().addLast(
                            new LoggingHandler(LogLevel.INFO),new HttpServerCodec());
                    ctx.channel().pipeline().addLast(new SimpleChannelInboundHandler<HttpRequest>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
                            DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(msg.protocolVersion(), HttpResponseStatus.OK);
                            byte[] content= res.get(msg.uri());
                            if(null==content) return;
                            defaultFullHttpResponse.headers().setInt(CONTENT_LENGTH,content.length);
                            defaultFullHttpResponse.content().writeBytes(content);
                            ctx.writeAndFlush(defaultFullHttpResponse);
                        }
                    });
                }
            }else {
                ChoiceStarGameNet.this.superInitChannel();
            }
            ctx.pipeline().remove(this);
            super.channelRead(ctx,msg);
        }
    }

}
