package com.github.dr.rwserver.net.web.realization.distribution;


import com.github.dr.rwserver.net.web.realization.MyControl;
import com.github.dr.rwserver.net.web.realization.agreement.ShareMessage;
import com.github.dr.rwserver.net.web.realization.constant.Config;
import com.github.dr.rwserver.net.web.realization.i.RequestManager;
import com.github.dr.rwserver.net.web.realization.tools.ShareAgreement;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class PlayServer extends SimpleChannelInboundHandler<Object> implements RequestManager {
	static final String NAME = "$_";
	MyControl control = new MyControl();
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {

	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// 链接激活将机器号发送过来
		// 准备开始发送心跳
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		// IO读发生
		ByteBuf br = (ByteBuf) msg;
		byte[] message = new byte[br.readableBytes()];
		br.readBytes(message);
		ShareAgreement share = new ShareAgreement();
		ShareMessage shareMessage = share.getMessage(message);// 接收网关进来的请求信息
		Attribute<Long> attribute = ctx.channel().attr(Config.HTTP_ID);
		attribute.set(shareMessage.getHttpId());
		boolean bm = control.my(shareMessage, this, ctx);
		/*if (!bm) {// 返回404
			WayMessage wayMessage = new WayMessage();
			wayMessage.setType(404);
			wayMessage.setHttpId(shareMessage.getHttpId());
			String way = JSONObject.toJSONString(wayMessage);
			response(ctx, way);
		}*/
		
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		//做集中抛错处理
	    long httpId =	ctx.channel().attr(Config.HTTP_ID).get();
	    /*WayMessage wayMessage = new WayMessage();
	    wayMessage.setHttpId(httpId);
	    wayMessage.setType(500);
	    String way = JSONObject.toJSONString(wayMessage);
		response(ctx, way);*/
		super.exceptionCaught(ctx, cause);
	}

	@Override
	public void response(ChannelHandlerContext ch, Object msg, byte httpCode) throws UnsupportedEncodingException {
		byte[] lm;
		if(msg instanceof  String){
			lm = msg.toString().getBytes(StandardCharsets.UTF_8);
		}else {
			lm = (byte[]) msg;
		}
		ByteBuf bu = Unpooled.buffer(lm.length);
		bu.writeBytes(lm);
		ch.writeAndFlush(bu).addListener(ChannelFutureListener.CLOSE);
	}
}
