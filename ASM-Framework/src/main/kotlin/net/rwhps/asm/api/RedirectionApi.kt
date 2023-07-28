/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */
package net.rwhps.asm.api

import net.rwhps.asm.RedirectionManagerImpl

object RedirectionApi {
    /**
     *  Not using a ServiceLoader for now, modularized environments with
     *  multiple ClassLoaders cause some issues.
     *
     * @return an implementation of the [RedirectionManager].
     */
    val redirectionManager: RedirectionManager = RedirectionManagerImpl()

    /**
     * [AllMethodsTransformer].
     *
     * @see Redirection
     */
    // used by the transformer
    @JvmStatic
    @Suppress("unused")
    @Throws(Throwable::class)
    operator fun invoke(obj: Any, desc: String, type: Class<*>, vararg args: Any?): Any? {
        return redirectionManager.invoke(obj, desc, type, *args)
    }
}
