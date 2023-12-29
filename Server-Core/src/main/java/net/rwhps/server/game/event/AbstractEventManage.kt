/*
 *
 *  * Copyright 2020-2023 RW-HPS Team and contributors.
 *  *
 *  * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  *
 *  * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 *
 */

package net.rwhps.server.game.event

import kotlinx.coroutines.*
import net.rwhps.server.game.event.core.EventListenerHost
import net.rwhps.server.util.ReflectionUtils
import net.rwhps.server.util.annotations.core.EventListenerHandler
import net.rwhps.server.util.concurrent.AbstractFuture
import net.rwhps.server.util.game.Events
import java.lang.reflect.Modifier
import java.util.concurrent.*
import java.util.concurrent.CancellationException
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * EventManage 的分别实现
 *
 * @date 2023/7/5 14:44
 * @author Dr (dr@der.kim)
 */
internal abstract class AbstractEventManage(private val eventClass: Class<*>) {
    protected val eventData = Events()
    private val eventRunScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)


    /**
     * 注册一个监听器
     *
     * @param eventListen EventListenerHost
     */
    fun registerListener(eventListen: EventListenerHost) {
        ReflectionUtils.getAllDeclaredMethods(eventListen::class.java).forEach { method ->
            method.getAnnotation(EventListenerHandler::class.java) ?: return@forEach
            val parameterTypes = method.parameterTypes
            if (parameterTypes.size == 1 && eventClass.isAssignableFrom(parameterTypes[0]) && !Modifier.isAbstract(
                        parameterTypes[0].modifiers
                )) {
                ReflectionUtils.makeAccessible(method)
                eventData.addEvent(parameterTypes[0]) { value ->
                    try {
                        ReflectionUtils.invokeMethod(method, eventListen, value)
                    } catch (e: Exception) {
                        eventListen.handleException(e)
                    }
                }
            }
        }
    }

    protected fun fire0(type: Any): AbstractFuture<*> {
        val lock = ReentrantLock()
        val condition = lock.newCondition()
        var errorObject: Throwable? = null

        return EventFuture(lock, condition, eventRunScope.async {
            lock.withLock {
                try {
                    eventData.fire(type::class.java, type)
                } catch (e: Exception) {
                    errorObject = e
                }
                condition.signalAll()
            }
        }).apply {
            this.errorObject = errorObject
        }
    }

    /**
     * 为 Event 提供同步支持
     *
     * 默认的 Event 运行在独立的线程中, 为了预防Plugin卡死Main/Net
     *
     * @date 2023/9/2 15:18
     * @author Dr (dr@der.kim)
     */
    protected class EventFuture<V>(
        private val lock: ReentrantLock,
        private val condition: Condition,
        private val future: Deferred<V>
    ) : AbstractFuture<V> {
        var errorObject: Throwable? = null

        override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
            future.cancel(CancellationException("Cancel"))
            return true
        }

        override fun isCancelled(): Boolean {
            return future.isCancelled
        }

        override fun isDone(): Boolean {
            return future.isCompleted
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun get(): V {
            return future.getCompleted()
        }

        @Throws(TimeoutException::class)
        override fun get(timeout: Long, unit: TimeUnit): V {
            if (await(timeout, unit)) {
                return get()
            }
            throw TimeoutException("Get Value TimeOut")
        }

        override fun cause(): Throwable? {
            return errorObject
        }

        @Throws(ExecutionException::class)
        override fun sync(): Future<V> {
            await()
            if (cause() != null) {
                throw ExecutionException(cause()!!)
            }
            return this
        }

        @Throws(InterruptedException::class)
        override fun await(): Future<V> {
            lock.withLock {
                condition.await()
            }
            return this
        }

        override fun awaitUninterruptible(): Future<V> {
            lock.withLock {
                condition.awaitUninterruptibly()
            }
            return this
        }

        @Throws(InterruptedException::class)
        override fun await(timeout: Long, unit: TimeUnit?): Boolean {
            lock.withLock {
                return condition.await(timeout, unit)
            }
        }
    }
}