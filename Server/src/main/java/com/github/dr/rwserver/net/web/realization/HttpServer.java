package com.github.dr.rwserver.net.web.realization;

import com.github.dr.rwserver.core.ex.Threads;
import com.github.dr.rwserver.net.web.realization.constant.Config;
import com.github.dr.rwserver.net.web.realization.constant.HttpsSetting;
import com.github.dr.rwserver.net.web.realization.scan.ScanControl;
import com.github.dr.rwserver.net.web.realization.session.SessionThread;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslHandler;

import java.net.InetSocketAddress;

public class HttpServer {
    public void start(int port, String rootUrl, int maxLength, String webSocektUrl,
                      WebSocketBack webSocketBack) {
        if (rootUrl != null && maxLength > 0) {
            ScanControl scanControl = new ScanControl();
            try {
                Config.setMessage_Max(maxLength);
                Config.setRootUrl(rootUrl);
                Config.setWebSocketUrl(webSocektUrl);
                scanControl.start(rootUrl);
                Threads.newThreadCore(SessionThread::new);
                connect(port, webSocketBack);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            System.out.println("服务参数异常，启动失败");
        }
    }

    private void connect(int port, WebSocketBack webSocketBack) {
        EventLoopGroup group = new NioEventLoopGroup();
        EventLoopGroup boss = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group, boss).channel(NioServerSocketChannel.class).childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new Em(webSocketBack));
            Channel nel = b.bind(new InetSocketAddress(port)).sync().channel();
            nel.closeFuture().sync();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }
}

class Em extends ChannelInitializer<SocketChannel> {
    private WebSocketBack webSocketBack;

    Em(WebSocketBack webSocketBack) {
        this.webSocketBack = webSocketBack;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        if (HttpsSetting.sslEnabled) {
            //务必放在第一位
            ch.pipeline().addLast("sslHandler", new SslHandler(HttpSslContextFactory.createSSLEngine()));
        }
        ch.pipeline().addLast("http-decoder", new HttpRequestDecoder());
        ch.pipeline().addLast(new HttpObjectAggregator(Config.getFileMaxLength()));
        ch.pipeline().addLast("http-encoder", new HttpResponseEncoder());
        ch.pipeline().addLast(new Http(webSocketBack));
    }

}