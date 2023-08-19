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

package net.rwhps.server.util.annotations.core

/**
 * 注解在 AbstractEvent/AbstractGlobalEvent 类上 : 表明事件是使用异步
 * 使用本注解也内涵: 这个事件对应接收器是只读的, 你不应该在接收器中干重型操作
 *
 * @author RW-HPS/Dr
 */
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(
        AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS
)
annotation class EventAsync