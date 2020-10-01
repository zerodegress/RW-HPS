package com.github.dr.rwserver.net;

import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.game.EventType;
import com.github.dr.rwserver.io.Packet;
import com.github.dr.rwserver.struct.OrderedMap;
import com.github.dr.rwserver.util.Events;
import com.github.dr.rwserver.util.PacketType;
import com.github.dr.rwserver.util.encryption.Sha;
import com.github.dr.rwserver.util.log.Log;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.ReferenceCountUtil;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.dr.rwserver.util.IsUtil.notIsBlank;


/**
 * @author Dr
 * @Data 2020/8/5 14:05:55
 */
public class Net {

	public static class NetStartGame {
		private static final OrderedMap<Channel, AbstractNetConnect> OVER_MAP = new OrderedMap<>(16);
		private final AcceptorIdleStateTrigger idleStateTrigger = new AcceptorIdleStateTrigger();

		public void StartGame(int port, String passwd) {
			if (notIsBlank(passwd)) {
				byte[] passwdShaArray = new Sha().sha256Arry(passwd);
				Data.game.passwd = String.format("%0" + (passwdShaArray.length * 2) + "X", new BigInteger(1, passwdShaArray));
			}
			try {
				Log.clog(Data.localeUtil.getinput("server.start.open"));
				this.openPort(port);
			} catch (Exception e) {
				Log.error("Net START", e);
			}
		}

		private void openPort(int port) throws InterruptedException {
			/* boss用来接收进来的连接 */
			EventLoopGroup bossGroup = new NioEventLoopGroup();
			/* workerGroup用来处理已经被接收的连接 */
			EventLoopGroup workerGroup = new NioEventLoopGroup();
			try {
				ServerBootstrap serverBootstrap = new ServerBootstrap();
				serverBootstrap.group(bossGroup, workerGroup)
						.channel(NioServerSocketChannel.class)
						.localAddress(new InetSocketAddress(port))
						.childHandler(new StartGameNet()).childOption(ChannelOption.SO_KEEPALIVE, true);
				ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
				Data.serverChannel = channelFuture.channel();
				Log.clog(Data.localeUtil.getinput("server.start.openPort"));
				Data.serverChannel.closeFuture().sync();
			} finally {
				bossGroup.shutdownGracefully();
				workerGroup.shutdownGracefully();
			}
		}

		@ChannelHandler.Sharable
		class StartGameNet extends ChannelInitializer<SocketChannel> {
			@Override
			protected void initChannel(SocketChannel socketChannel) throws Exception {
				ChannelPipeline pipeline = socketChannel.pipeline();
				pipeline.addLast(new IdleStateHandler(0, 6, 0, TimeUnit.SECONDS));
				pipeline.addLast(idleStateTrigger);
				pipeline.addLast(new NewDecoder());
				pipeline.addLast(new NewServerHandler());
			}

		}

		@ChannelHandler.Sharable
		class AcceptorIdleStateTrigger extends ChannelInboundHandlerAdapter {
			@Override
			public void channelActive(ChannelHandlerContext ctx) throws Exception {
				ctx.fireChannelActive();
			}

			@Override
			public void channelInactive(ChannelHandlerContext ctx) throws Exception {
				System.out.println("停止时间是：" + new Date());
				clear(ctx);
				ctx.close();
			}

			@Override
			public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
				if (Data.game.isStartGame) {
					return;
				}
				if (evt instanceof IdleStateEvent) {
					IdleState state = ((IdleStateEvent) evt).state();
					if (state == IdleState.WRITER_IDLE) {
						Channel channel = ctx.channel();
						AbstractNetConnect con = OVER_MAP.get(channel);
						if (con == null) {
							ctx.close();
						}
						if (con.getTry() >= Data.SERVER_MAX_TRY) {
							clear(ctx);
							ctx.close();
						} else {
							con.setTry();
						}
					}
				} else {
					super.userEventTriggered(ctx, evt);
				}
			}
		}

		class NewDecoder extends ByteToMessageDecoder {
			private static final int HEADER_SIZE = 8;

			@Override
			protected void decode(ChannelHandlerContext ctx, ByteBuf bufferIn, List<Object> out) throws Exception {
				if (bufferIn == null) {
					return;
				}
				if (bufferIn.readableBytes() < HEADER_SIZE) {
					Log.error("错误的消息");
					return;
				}
				//消息长度
				int begin = bufferIn.readerIndex();
				int contentLength = bufferIn.readInt();
				if (contentLength > 40960) {
					Log.error("MAX Packet");
					ReferenceCountUtil.release(bufferIn);
					ctx.close();
					return;
				}
				int type = bufferIn.readInt();
				if (bufferIn.readableBytes() < contentLength) {
					Log.clog("当前 网络不稳定 消息长度接受错误");
					Log.clog(contentLength + "  " + type);
					Log.clog("消息的内容长度没有达到预期设定的长度，还原指针重新读");
					bufferIn.readerIndex(begin);
					return;
				}
				byte[] b = new byte[contentLength];
				bufferIn.readBytes(b);
				Log.debug(type, ctx.channel());
				Log.debug(type, contentLength);
				out.add(new Packet(type, b));
			}

			@Override
			public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
				ctx.flush();
			}
		}

		class NewServerHandler extends SimpleChannelInboundHandler<Object> {
			@Override
			protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
				if (msg instanceof Packet) {
					Packet p = (Packet) msg;
					Channel channel = ctx.channel();
					AbstractNetConnect con = OVER_MAP.get(channel);
					if (con == null) {
						con = Data.game.connectNet.getVersionNet(channel.remoteAddress());
						OVER_MAP.put(channel, con);
					}
					con.setChannel(channel);
					try {
						switch (p.type) {
							// 连接服务器
							case PacketType.PACKET_PREREGISTER_CONNECTION:
								con.registerConnection(p);
								break;
							// 注册用户
							case PacketType.PACKET_PLAYER_INFO:
								if (con.getPlayerInfo(p)) {

								} else {
									con.disconnect();
								}
								break;
							case PacketType.PACKET_HEART_BEAT_RESPONSE:
								Player player = con.getPlayer();
								player.ping = (int) (System.currentTimeMillis() - player.timeTemp) >> 1;
								//心跳 懒得处理
								break;
							// 玩家发送消息
							case PacketType.PACKET_ADD_CHAT:
								con.receiveChat(p);
								break;
							// 玩家主动断开连接
							case PacketType.PACKET_DISCONNECT:
								con.disconnect();
								break;
							case PacketType.PACKET_ACCEPT_START_GAME:
								con.getPlayer().start = true;
								break;
							// ?
							case PacketType.PACKET_ADD_GAMECOMMAND:
								con.receiveCommand(p);
								break;
							default:
								break;
						}
					} catch (Exception e) {
						if (con.getTry() >= Data.SERVER_MAX_TRY) {
							clear(ctx);
						} else {
							con.setTry();
						}
					} finally {
						//ReferenceCountUtil.release(msg);
					}
				}
			}

			@Override
			public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
				Log.error(cause);
				ctx.close();
			}
		}

		private void clear(ChannelHandlerContext ctx) {
			Channel channel = ctx.channel();
			AbstractNetConnect con = OVER_MAP.get(channel);
			if (con != null) {
				Events.fire(new EventType.PlayerLeave(con.getPlayer()));
				con.disconnect();
			}
			OVER_MAP.remove(channel);
		}
	}
}