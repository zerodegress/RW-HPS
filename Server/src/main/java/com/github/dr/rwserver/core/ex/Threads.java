package com.github.dr.rwserver.core.ex;

import java.util.concurrent.*;

public class Threads {

	private static final ExecutorService Core 							= Executors.newFixedThreadPool(3);
	private static final ScheduledExecutorService Service 				= Executors.newScheduledThreadPool(6);
	private static final ThreadPoolExecutor Player_Heat_Thread 			= new ThreadPoolExecutor(8,8,1,TimeUnit.MINUTES,new LinkedBlockingDeque<Runnable>(10));
	private static final ExecutorService singleThreadExecutor 			= Executors.newSingleThreadExecutor();

	/*
	private static ScheduledFuture THREAD_TIME;
	Player_Thread.shutdown();
	Player_Thread = new ThreadPoolExecutor(Data.game.maxPlayer,Data.game.maxPlayer,1, TimeUnit.MINUTES,new LinkedBlockingDeque<Runnable>(20));
	 */


	public static void playerThread() {
		//
	}

	public static void close() {
		Core.shutdownNow();
		Service.shutdownNow();
		Player_Heat_Thread.shutdownNow();
	}

	public static ScheduledFuture newThreadService(Runnable run,int endTime,TimeUnit timeUnit) {
		return Service.schedule(run,endTime,timeUnit);
	}

	public static ScheduledFuture newThreadService2(Runnable run,int startTime,int endTime,TimeUnit timeUnit) {
		return Service.scheduleAtFixedRate(run,startTime,endTime,timeUnit);
	}

	public static void newThreadPlayer1(Runnable run) {
		singleThreadExecutor.execute(run);
	}

	public static void newThreadPlayerHeat(Runnable run) {
    	Player_Heat_Thread.execute(run);
	}

	public static void newThreadCore(Runnable run) {
		Core.execute(run);
	}
}