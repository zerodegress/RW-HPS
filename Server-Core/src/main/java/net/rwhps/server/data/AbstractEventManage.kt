/*
 *
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 *
 */

package net.rwhps.server.data

import net.rwhps.server.game.event.core.EventListenerHost
import net.rwhps.server.util.ReflectionUtils
import net.rwhps.server.util.annotations.core.EventListenerHandler
import net.rwhps.server.util.game.Events
import java.lang.reflect.Modifier

/**
 * EventManage 的分别实现
 *
 * @date 2023/7/5 14:44
 * @author RW-HPS/Dr
 */
internal abstract class AbstractEventManage(private val eventClass: Class<*>) {
    protected val eventData = Events()

    fun registerListener(eventListen: EventListenerHost) {
        ReflectionUtils.getAllDeclaredMethods(eventListen::class.java).forEach { method ->
            method.getAnnotation(EventListenerHandler::class.java) ?: return@forEach
            val parameterTypes = method.parameterTypes
            if (parameterTypes.size == 1 && eventClass.isAssignableFrom(parameterTypes[0]) && !Modifier.isAbstract(parameterTypes[0].modifiers)) {
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
}