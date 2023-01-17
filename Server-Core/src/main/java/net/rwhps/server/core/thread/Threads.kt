/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.core.thread

import net.rwhps.server.struct.Seq
import net.rwhps.server.util.Time
import net.rwhps.server.util.threads.GetNewThreadPool
import net.rwhps.timetask.task.TimeTaskManage
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

/**
 * TimeTask / Thread Core
 * @author RW-HPS/Dr
 */
object Threads {
    private val CORE_THREAD: ExecutorService = GetNewThreadPool.getNewFixedThreadPool(3, "Core-")
    private var CORE_NET_THREAD: ExecutorService = GetNewThreadPool.getNewFixedThreadPool(3, "Core-Net-")
    private val SERVICE = TimeTaskManage() // 10 Thread
    private val PLAYER_HEAT_THREAD = GetNewThreadPool.getNewFixedThreadPool(10, "Core-Heat-")

    /** Execute runnable on exit  */
    private val SAVE_POOL = Seq<Runnable>()

    @JvmStatic
	fun close() {
        CORE_THREAD.shutdownNow()
        CORE_NET_THREAD.shutdownNow()
        SERVICE.shutdownNow()
        PLAYER_HEAT_THREAD.shutdownNow()
    }

    @JvmStatic
    fun closeNet() {
        CORE_NET_THREAD.shutdownNow()
        CORE_NET_THREAD = GetNewThreadPool.getNewFixedThreadPool(3, "Core-Net-")
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
        SAVE_POOL.eachAll { obj: Runnable -> obj.run() }
    }


    /**
     * Create a countdown timer
     * @param run       Runnable
     * @param endTime   How long to execute
     * @param timeUnit  Time Unit
     * @param taskFlag  NameID
     * @return          ScheduledFuture<*>
     */
    @JvmStatic
    internal fun newCountdown(taskFlag: CallTimeTask, endTime: Int, timeUnit: TimeUnit, run: Runnable) {
        newCountdown(taskFlag.name,taskFlag.group,taskFlag.description, endTime,timeUnit, run)
    }

    /**
     * 创建一个倒数计时器
     * @param taskFlagName          Name
     * @param taskFlagGroup         Group
     * @param taskFlagDescription   Description
     * @param endTime               How long to execute
     * @param timeUnit              Time Unit
     * @param run                   Runnable
     */
    @JvmStatic
    fun newCountdown(taskFlagName: String, taskFlagGroup: String, taskFlagDescription: String, endTime: Int, timeUnit: TimeUnit, run: Runnable) {
        SERVICE.addCountdown(
            taskFlagName,taskFlagGroup,taskFlagDescription,
            Time.concurrentMillis() + TimeUnit.MILLISECONDS.convert(endTime.toLong(),timeUnit),
            run
        )
    }

    /**
     * 创建一个定时计时器
     * @param run Runnable
     * @param startTime 多长时间后开始
     * @param intervalTime 执行间隔
     * @param timeUnit Time Unit
     * @param taskFlag NameID
     * @return ScheduledFuture<*>
     */
    @JvmStatic
    fun newTimedTask(taskFlag: CallTimeTask, startTime: Int, intervalTime: Int, timeUnit: TimeUnit, run: Runnable) {
        newTimedTask(taskFlag.name,taskFlag.group,taskFlag.description, startTime, intervalTime,timeUnit, run)
    }

    /**
     * 创建一个倒数计时器
     * @param taskFlagName          Name
     * @param taskFlagGroup         Group
     * @param taskFlagDescription   Description
     * @param startTime             How long to execute
     * @param intervalTime          Repeat after time interval
     * @param timeUnit              Time Unit
     * @param run                   Runnable
     */
    @JvmStatic
    fun newTimedTask(taskFlagName: String, taskFlagGroup: String, taskFlagDescription: String, startTime: Int, intervalTime: Int, timeUnit: TimeUnit, run: Runnable) {
        SERVICE.addTimedTask(
            taskFlagName,taskFlagGroup,taskFlagDescription,
            Time.concurrentMillis() + TimeUnit.MILLISECONDS.convert(startTime.toLong(),timeUnit),
            TimeUnit.MILLISECONDS.convert(intervalTime.toLong(),timeUnit),
            run
        )
    }

    /**
     * Check if there is a task
     * @param taskFlag NameID
     * @return Boolean
     */
    @JvmStatic
    internal fun containsTimeTask(taskFlag: CallTimeTask): Boolean {
        return containsTimeTask(taskFlag.name,taskFlag.group)
    }

    /**
     * Check if there is a task
     * @param taskFlagName          Name
     * @param taskFlagGroup         Group
     * @return Boolean
     */
    @JvmStatic
    fun containsTimeTask(taskFlagName: String, taskFlagGroup: String): Boolean {
        return SERVICE.contains(taskFlagName,taskFlagGroup)
    }

    /**
     * Close Task
     * @param taskFlag NameID
     * @param run Runnable?
     * @return Boolean
     */
    @JvmStatic
    @JvmOverloads
    fun closeTimeTask(taskFlag: CallTimeTask, run: Runnable? = null): Boolean {
        return closeTimeTask(taskFlag.name,taskFlag.group,run)
    }

    /**
     * Close Task
     * @param taskFlagName          Name
     * @param taskFlagGroup         Group
     * @param run Runnable?
     * @return Boolean
     */
    @JvmStatic
    @JvmOverloads
    fun closeTimeTask(taskFlagName: String, taskFlagGroup: String, run: Runnable? = null): Boolean {
        val flag =  SERVICE.remove(taskFlagName,taskFlagGroup)
        if (flag) {
            run?.run()
        }
        return flag
    }
}