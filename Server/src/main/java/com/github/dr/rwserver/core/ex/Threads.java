package com.github.dr.rwserver.core.ex;

import java.util.concurrent.*;

/**
 * @author Dr
 */
public class Threads {

	private static final ExecutorService CORE_THREAD 					= Executors.newFixedThreadPool(5);
	private static final ExecutorService CORE_NET_THREAD 				= Executors.newFixedThreadPool(1);
	private static final ScheduledExecutorService SERVICE 				= Executors.newScheduledThreadPool(8);
	private static final ThreadPoolExecutor PLAYER_HEAT_THREAD 			= new ThreadPoolExecutor(8,8,1,TimeUnit.MINUTES, new LinkedBlockingDeque<>(10));
	private static final ExecutorService SINGLE_THREAD_EXECUTOR 		= Executors.newSingleThreadExecutor();
	private static final ExecutorService SINGLE_UDP_THREAD_EXECUTOR 		= Executors.newSingleThreadExecutor();

	/*
	private static ScheduledFuture THREAD_TIME;
	Player_Thread.shutdown();
	Player_Thread = new ThreadPoolExecutor(Data.game.maxPlayer,Data.game.maxPlayer,1, TimeUnit.MINUTES,new LinkedBlockingDeque<Runnable>(20));
	 */


	public static void playerThread() {
		//
	}

	public static void close() {
		CORE_THREAD.shutdownNow();
		CORE_NET_THREAD.shutdownNow();
		SERVICE.shutdownNow();
		PLAYER_HEAT_THREAD.shutdownNow();
	}

	public static void closeNet() {
		CORE_NET_THREAD.shutdownNow();
	}

	public static ScheduledFuture newThreadService(Runnable run,int endTime,TimeUnit timeUnit) {
		return SERVICE.schedule(run,endTime,timeUnit);
	}

	public static ScheduledFuture newThreadService2(Runnable run,int startTime,int endTime,TimeUnit timeUnit) {
		return SERVICE.scheduleAtFixedRate(run,startTime,endTime,timeUnit);
	}

	public static void newThreadPlayer1(Runnable run) {
		SINGLE_THREAD_EXECUTOR.execute(run);
	}
	public static void newThreadPlayer2(Runnable run) {
		SINGLE_UDP_THREAD_EXECUTOR.execute(run);
	}

	public static void newThreadPlayerHeat(Runnable run) {
		PLAYER_HEAT_THREAD.execute(run);
	}

	public static void newThreadCore(Runnable run) {
		CORE_THREAD.execute(run);
	}

	public static void newThreadCoreNet(Runnable run) {
		CORE_NET_THREAD.execute(run);
	}
}