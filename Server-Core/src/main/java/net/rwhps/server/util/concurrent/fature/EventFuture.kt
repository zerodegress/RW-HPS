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

package net.rwhps.server.util.concurrent.fature

import kotlinx.coroutines.*
import net.rwhps.server.util.log.exp.ImplementedException
import java.util.concurrent.*
import java.util.concurrent.CancellationException

/**
 * 为 Event 提供同步支持
 *
 * 默认的 Event 运行在独立的线程中, 为了预防Plugin卡死Main/Net
 *
 * @date 2023/9/2 15:18
 * @author Dr (dr@der.kim)
 */
class EventFuture<V>(
    private val future: Deferred<V>
) : AbstractFuture<V> {
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

    override fun get(): V? {
        throw ImplementedException("No")
    }

    @Throws(TimeoutException::class)
    override fun get(timeout: Long, unit: TimeUnit): V {
        throw ImplementedException("No")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun cause(): Throwable? {
        return if (future.isCompleted) {
            future.getCompleted() as Throwable?
        } else {
            null
        }
    }

    @Throws(ExecutionException::class)
    override fun sync() {
        await()
        if (cause() != null) {
            throw ExecutionException(cause()!!)
        }
    }

    @Throws(InterruptedException::class)
    override fun await() {
        runBlocking {
            future.await()
        }
    }

    override fun awaitUninterruptible() {
        runBlocking {
            try {
                future.await()
            } catch (_: InterruptedException) {
                // No use
            } catch (_: CancellationException) {
                // No use
            }
        }
    }

    @Throws(InterruptedException::class)
    override fun await(timeout: Long, unit: TimeUnit?): Boolean {
        runBlocking {
            try {
                withTimeout(TimeUnit.MILLISECONDS.convert(timeout, unit)) {
                    future.await()
                    return@withTimeout true
                }.let {
                    return@runBlocking it
                }
            } catch (e: TimeoutCancellationException) {
                return@runBlocking false
            }
        }.let {
            return it
        }
    }
}