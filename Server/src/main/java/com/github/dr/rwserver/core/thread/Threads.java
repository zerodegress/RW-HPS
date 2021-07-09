package com.github.dr.rwserver.core.thread;

import com.github.dr.rwserver.struct.OrderedMap;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.IsUtil;
import com.github.dr.rwserver.util.threads.GetNewThredPool;
import com.github.dr.rwserver.util.threads.ScheduledThreadPoolExecutorDynamic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Dr
 */
public class Threads {

	private static final ExecutorService CORE_THREAD 					= GetNewThredPool.getNewFixedThreadPool(6,"Core-");
	private static final ExecutorService CORE_NET_THREAD 				= GetNewThredPool.getNewFixedThreadPool(1,"Core-Net-");
	private static final ScheduledThreadPoolExecutorDynamic SERVICE 	= GetNewThredPool.getNewScheduledThreadDynamicPool(10,"ScheduledExecutorPool-");
	private static final ThreadPoolExecutor PLAYER_HEAT_THREAD 			= GetNewThredPool.getNewFixedThreadPool(10,"Core-Heat-");
	/** 在退出时执行Runnable */
	private static final Seq<Runnable> SAVE_POOL 						= new Seq<>();
	private static final OrderedMap<String,ScheduledFuture<?>> ScheduledFutureData = new OrderedMap<>();

	public static void close() {
		CORE_THREAD.shutdownNow();
		CORE_NET_THREAD.shutdownNow();
		SERVICE.shutdownNow();
		PLAYER_HEAT_THREAD.shutdownNow();
	}

	public static void closeNet() {
		CORE_NET_THREAD.shutdownNow();
	}

	/**
	 * 创建一个倒数计时器
	 * @param run Runnable
	 * @param endTime 多少时间后执行
	 * @param timeUnit 时间单位
	 */
	public static void newThreadService(Runnable run,int endTime,TimeUnit timeUnit,String nameID) {
		ScheduledFutureData.put(nameID,SERVICE.schedule(run,endTime,timeUnit));
	}

	public static void newThreadService2(Runnable run,int startTime,int endTime,TimeUnit timeUnit,String nameID) {
		ScheduledFutureData.put(nameID,SERVICE.scheduleAtFixedRate(run,startTime,endTime,timeUnit));
	}//!Threads.getIfScheduledFutureData("AfkCountdown")

	public static void removeScheduledFutureData(String name) {
		ScheduledFuture<?> scheduledFuture = ScheduledFutureData.get(name);
		if (IsUtil.notIsBlank(scheduledFuture)) {
			SERVICE.cancelSchedule(scheduledFuture);
			ScheduledFutureData.remove(name);
		}
	}

	public static boolean getIfScheduledFutureData(String name) {
		return ScheduledFutureData.containsKey(name);
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