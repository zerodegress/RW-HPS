/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.core.thread

import com.github.dr.rwserver.struct.OrderedMap
import com.github.dr.rwserver.struct.Seq
import com.github.dr.rwserver.util.IsUtil
import com.github.dr.rwserver.util.alone.annotations.NeedHelp
import com.github.dr.rwserver.util.alone.annotations.NeedToRefactor
import com.github.dr.rwserver.util.threads.GetNewThreadPool
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.RunnableScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * @author Dr
 */
@NeedHelp
@NeedToRefactor
object Threads {
    private val CORE_THREAD: ExecutorService = GetNewThreadPool.getNewFixedThreadPool(6, "Core-")
    private val CORE_NET_THREAD: ExecutorService = GetNewThreadPool.getNewFixedThreadPool(1, "Core-Net-")
    private val SERVICE = GetNewThreadPool.getNewScheduledThreadPool(10, "ScheduledExecutorPool-")
    private val PLAYER_HEAT_THREAD = GetNewThreadPool.getNewFixedThreadPool(10, "Core-Heat-")

    /** Execute runnable on exit  */
    private val SAVE_POOL = Seq<Runnable>()
    private val SCHEDULED_FUTURE_DATA = OrderedMap<String, RunnableScheduledFuture<*>>()
    private val TASK_FUTURE_DATA = OrderedMap<String, Timer>()

    @JvmStatic
	fun close() {
        CORE_THREAD.shutdownNow()
        CORE_NET_THREAD.shutdownNow()
        SERVICE.shutdownNow()
        PLAYER_HEAT_THREAD.shutdownNow()
    }

    fun closeNet() {
        CORE_NET_THREAD.shutdownNow()
    }

    /**
     * 创建一个倒数计时器
     * @param run Runnable
     * @param endTime 多少时间后执行
     * @param timeUnit 时间单位
     * @param nameId NameID
     */
	@JvmStatic
    fun newThreadService(run: Runnable, endTime: Int, timeUnit: TimeUnit, nameId: String) {
        SCHEDULED_FUTURE_DATA.put(nameId, SERVICE.schedule(run, endTime.toLong(), timeUnit) as RunnableScheduledFuture<*>)
    }

    /**
     * 创建一个定时计时器
     * @param run Runnable
     * @param startTime 多长时间后开始
     * @param endTime 执行间隔
     * @param timeUnit 时间单位
     * @param nameId NameID
     */
	@JvmStatic
	fun newThreadService2(run: Runnable, startTime: Int, endTime: Int, timeUnit: TimeUnit, nameId: String) {
        SCHEDULED_FUTURE_DATA.put(nameId, SERVICE.scheduleAtFixedRate(run, startTime.toLong(), endTime.toLong(), timeUnit) as RunnableScheduledFuture<*>)
        //Log.error((SERVICE as ScheduledThreadPoolExecutor).getQueue().size)
    }

    @JvmStatic
	fun removeScheduledFutureData(nameId: String) {
        val scheduledFuture = SCHEDULED_FUTURE_DATA[nameId]
        if (IsUtil.notIsBlank(scheduledFuture)) {
            scheduledFuture.cancel(true)
            SCHEDULED_FUTURE_DATA.remove(nameId)
        }
    }

    @JvmStatic
	fun getIfScheduledFutureData(name: String): Boolean {
        return SCHEDULED_FUTURE_DATA.containsKey(name)
    }

    @JvmStatic
    fun newThreadPlayerHeat(run: Runnable) {
        PLAYER_HEAT_THREAD.execute(run)
    }

    @JvmStatic
	fun newThreadCore(run: Runnable) {
        CORE_THREAD.execute(run)
    }

    @JvmStatic
    fun newThreadCoreNet(run: Runnable) {
        CORE_NET_THREAD.execute(run)
    }

    @JvmStatic
	fun addSavePool(run: Runnable) {
        SAVE_POOL.add(run)
    }

    @JvmStatic
	fun runSavePool() {
        SAVE_POOL.each { obj: Runnable -> obj.run() }
    }
}