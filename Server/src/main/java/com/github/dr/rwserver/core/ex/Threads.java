package com.github.dr.rwserver.core.ex;

import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.threads.ThreadFactoryName;

import java.util.concurrent.*;

/**
 * @author Dr
 */
public class Threads {

	private static final ExecutorService CORE_THREAD 					= new ThreadPoolExecutor(6, 6, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), ThreadFactoryName.nameThreadFactory("Core-"));
	private static final ExecutorService CORE_NET_THREAD 				= Executors.newFixedThreadPool(1);
	private static final ScheduledExecutorService SERVICE 				= Executors.newScheduledThreadPool(10);
	private static final ThreadPoolExecutor PLAYER_HEAT_THREAD 			= new ThreadPoolExecutor(8,8,1,TimeUnit.MINUTES, new LinkedBlockingDeque<>(10));
	private static final Seq<Runnable> SAVE_POOL 						= new Seq<>();



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

	public static void newThreadPlayerHeat(Runnable run) {
		PLAYER_HEAT_THREAD.execute(run);
	}

	public static void newThreadCore(Runnable run) {
		CORE_THREAD.execute(run);
	}

	public static void newThreadCoreNet(Runnable run) {
		CORE_NET_THREAD.execute(run);
	}

	public static void addSavePool(Runnable run) {
		SAVE_POOL.add(run);
	}

	public static void runSavePool() {
		SAVE_POOL.each(Runnable::run);
	}

}