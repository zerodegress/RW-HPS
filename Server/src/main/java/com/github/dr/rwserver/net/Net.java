package com.github.dr.rwserver.net;

import com.github.dr.rwserver.core.Core;
import com.github.dr.rwserver.core.ex.Threads;
import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.io.Packet;
import com.github.dr.rwserver.net.udp.ReliableServerSocket;
import com.github.dr.rwserver.net.web.realization.HttpServer;
import com.github.dr.rwserver.net.web.realization.constant.HttpsSetting;
import com.github.dr.rwserver.struct.OrderedMap;
import com.github.dr.rwserver.util.PacketType;
import com.github.dr.rwserver.util.Time;
import com.github.dr.rwserver.util.alone.BlackList;
import com.github.dr.rwserver.util.encryption.Sha;
import com.github.dr.rwserver.util.log.Log;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.ReferenceCountUtil;

import java.io.DataInputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.github.dr.rwserver.util.IsUtil.notIsBlank;


/**
 * @author Dr
 * @Data 2020/8/5 14:05:55
 */
public class Net {
	public static class NetStartGame {
		private static final OrderedMap<SocketAddress, AbstractNetConnect> OVER_MAP = new OrderedMap<>(16);
		private final AcceptorIdleStateTrigger idleStateTrigger = new AcceptorIdleStateTrigger();
		private final BlackList blackList = new BlackList();

		public void startGame(int port, String passwd) {
			if (notIsBlank(passwd)) {
				Data.game.passwd = new BigInteger(1, new Sha().sha256Arry(passwd)).toString(16).toUpperCase(Locale.ROOT);
			}
			try {
				Log.clog(Data.localeUtil.getinput("server.start.open"));
				if (Data.config.readBoolean("UDPSupport",false)) {
					Threads.newThreadCoreNet(() -> {
						try(ServerSocket serverSocket = new ReliableServerSocket(port)) {
							final ExecutorService group = Executors.newFixedThreadPool(10);
							do {
								final Socket socket = serverSocket.accept();
								final SocketAddress sockAds = socket.getRemoteSocketAddress();
								AbstractNetConnect con = OVER_MAP.get(sockAds);
								if (con == null) {
									con = Data.game.connectNet.getVersionNet(null);
									OVER_MAP.put(sockAds, con);
									con.setConnectionAgreement(new ConnectionAgreement(socket));
								}
								final AbstractNetConnect conFinal = con;
								group.execute(() -> {
									while (!socket.isClosed()) {
										try {
											DataInputStream in = new DataInputStream(socket.getInputStream());
											int size = in.readInt();
											int type = in.readInt();
											byte[] bytes = new byte[size];
											int bytesRead = 0;
											while (bytesRead < size) {
												int readIn = in.read(bytes, bytesRead, size - bytesRead);
												if (readIn == -1) {
													break;
												}
												bytesRead += readIn;
											}
											typeConnect(null,conFinal, new Packet(type,bytes));
										} catch (Exception e) {
											Log.error("UDP READ", e);
											break;
										}
									}
									conFinal.disconnect();
									OVER_MAP.remove(sockAds);
								});
							} while (true);
						} catch (Exception ignored) {
						}
					});
				}
				this.openPort(port);

			} catch (Exception e) {
				Log.error("Net START", e);
				Core.exit();
			}
		}

		private void openPort(int port) throws InterruptedException {
			/* boss用来接收进来的连接 */
			//EpollEventLoopGroup bossGroup = new EpollEventLoopGroup(1);
			EpollEventLoopGroup bossGroup = new EpollEventLoopGroup(1);
			/* workerGroup用来处理已经被接收的连接 */
			EpollEventLoopGroup workerGroup = new EpollEventLoopGroup();
			try {
				ServerBootstrap serverBootstrapTcp = new ServerBootstrap();
				serverBootstrapTcp.group(bossGroup, workerGroup)
								  .channel(EpollServerSocketChannel.class)
								  .localAddress(new InetSocketAddress(port))
								  .childOption(ChannelOption.SO_KEEPALIVE, true)
								  .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
								  .childHandler(new StartGameNetTcp());

				ChannelFuture channelFutureTcp = serverBootstrapTcp.bind(port).sync();
				Data.serverChannelB = channelFutureTcp.channel();
				Data.config.setObject("runPid",Data.core.getPid());
				Data.config.save();
				Log.clog(Data.localeUtil.getinput("server.start.openPort"));
				if (Data.game.webApi) {
					HttpServer httpServer = new HttpServer();
					HttpsSetting.sslEnabled = Data.game.webApiSsl;
					HttpsSetting.keystorePath = Data.game.webApiSslKetPath;
					HttpsSetting.certificatePassword = Data.game.webApiSslPasswd;
					HttpsSetting.keystorePassword =  Data.game.webApiSslPasswd;
					httpServer.start(Data.game.webApiPort, "com.github.dr.rwserver.net.web.api", 1024,
							null, null);
				}
				Data.serverChannelB.closeFuture().sync();
			} finally {
				bossGroup.shutdownGracefully();
				workerGroup.shutdownGracefully();
			}
		}

		@ChannelHandler.Sharable
		class StartGameNetTcp extends ChannelInitializer<SocketChannel> {
			@Override
			protected void initChannel(SocketChannel socketChannel) throws Exception {
				ChannelPipeline pipeline = socketChannel.pipeline();
				pipeline.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
				pipeline.addLast(idleStateTrigger);
				pipeline.addLast(new ByteToMessageDecoder() {
					private static final int HEADER_SIZE = 8;

					@Override
					protected void decode(ChannelHandlerContext ctx, ByteBuf bufferIn, List<Object> out) throws Exception {
						final String addSock = ctx.channel().remoteAddress().toString();
						final String ip = addSock.substring(1, addSock.indexOf(':'));
						if (blackList.containsBlackList(ip)) {
							ReferenceCountUtil.release(bufferIn);
							ctx.close();
						}
						if (bufferIn == null) {
							return;
						}
						if (bufferIn.readableBytes() < HEADER_SIZE) {
							//Log.error("错误的消息");
							return;
						}
						//消息长度

						//final int maxContentLength = 40960;
						// 10MB
						final int maxContentLength = 10485760;
						if (bufferIn.readableBytes() > maxContentLength) {
							Log.error("MAX Packet");
							ReferenceCountUtil.release(bufferIn);
							blackList.addBlackList(ip);
							Log.warn("BlackList", ip);
							ctx.close();
							return;
						}
						final int begin = bufferIn.readerIndex();
						final int contentLength = bufferIn.readInt();
						int type = bufferIn.readInt();
						if (bufferIn.readableBytes() < contentLength) {
							//Log.clog("当前 网络不稳定 消息长度接受错误");
							//Log.clog(contentLength + "  " + type);
							//Log.clog("消息的内容长度没有达到预期设定的长度，还原指针重新读");
							bufferIn.readerIndex(begin);
							return;
						}
						byte[] b = new byte[contentLength];
						bufferIn.readBytes(b);
						out.add(new Packet(type, b));
					}

					@Override
					public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
						ctx.flush();
					}
				}).addLast(new NewServerHandler());
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
				Log.info("停止时间是：" + new Date());
				clear(ctx);
			}

			@Override
			public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
				if (evt instanceof IdleStateEvent) {
					IdleState state = ((IdleStateEvent) evt).state();
					if (state == IdleState.WRITER_IDLE) {
						Channel channel = ctx.channel();
						AbstractNetConnect con = OVER_MAP.get(channel.remoteAddress());
						if (con == null) {
							clear(ctx);
							return;
						}
						if (con.getIsPasswd()) {
							/* 60s无反应判定close */
							if (Time.concurrentMillis() > (con.getLastReceivedTime()+60 * 1000L)) {
								clear(ctx);
							}
						} else {
							if (Time.concurrentMillis() > (con.getLastReceivedTime()+180 * 1000L)) {
								clear(ctx);
							}
						}

					}
				} else {
					super.userEventTriggered(ctx, evt);
				}
			}

		}

		final class NewServerHandler extends SimpleChannelInboundHandler<Object> {
			@Override
			protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
				if (msg instanceof Packet) {
					final Packet p = (Packet) msg;
					final Channel channel = ctx.channel();
					final SocketAddress sockAds = channel.remoteAddress();
					AbstractNetConnect con = OVER_MAP.get(sockAds);
					if (con == null) {
						con = Data.game.connectNet.getVersionNet(ctx.alloc());
						OVER_MAP.put(sockAds, con);
						con.setConnectionAgreement(new ConnectionAgreement(channel));
					}
					final AbstractNetConnect finalCon = con;
					ctx.executor().execute(() -> {
						try {
							typeConnect(channel,finalCon, p);
						} catch (Exception e) {
							clear(ctx);
						} finally {
							ReferenceCountUtil.release(msg);
						}
					});
				}
			}

			@Override
			public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
				Log.error(cause);
				ctx.close();
			}
		}

		private void typeConnect(Channel channel,AbstractNetConnect con,Packet p) throws Exception {
			try {
				netRwHps(con,p);
			} catch (Exception e) {
				if (con.getTry() >= Data.SERVER_MAX_TRY) {
					throw e;
				} else {
					con.setTry();
				}
				Log.error(e);
			}
		}

		private void netRwHps(final AbstractNetConnect con,final Packet p) throws Exception {
				// CPU分支预测
				con.setLastReceivedTime(Time.concurrentMillis());
				if (p.type == PacketType.PACKET_ADD_GAMECOMMAND) {
					con.receiveCommand(p);
					con.getPlayer().lastMoveTime = Time.millis();
				} else {
					switch (p.type) {
						// 连接服务器
						case PacketType.PACKET_PREREGISTER_CONNECTION:
							con.registerConnection(p);
							break;
						// 注册用户
						case PacketType.PACKET_PLAYER_INFO:
							if (!con.getPlayerInfo(p)) {
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
						case PacketType.PACKET_SERVER_DEBUG:
							con.debug(p);
							break;
						case PacketType.PACKET_SYNC:
							Data.game.gameSaveCache = p;
							break;
						default:
							break;
					}
			}
		}

		private void clear(ChannelHandlerContext ctx) {
			Channel channel = ctx.channel();
			AbstractNetConnect con = OVER_MAP.get(channel.remoteAddress());
			if (con != null) {
				try {
					con.disconnect();
					ctx.close();
				} catch (Exception e) {
					Log.info(e);
				}
			}
			OVER_MAP.remove(channel.remoteAddress());
		}

		//private void netType()
	}
}