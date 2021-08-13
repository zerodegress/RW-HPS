/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.util.io

import java.io.*
import java.nio.charset.StandardCharsets

/**
 * 流转换
 * @author Dr
 */
object IoReadConversion {
    @JvmStatic
    fun streamBufferRead(inputStream: InputStream): BufferedReader {
        return BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
    }

    @JvmStatic
    @Throws(IOException::class)
    fun fileToBufferReadStream(file: File): BufferedReader {
        return BufferedReader(fileToReadStream(file))
    }

    @JvmStatic
    @Throws(IOException::class)
    fun fileToReadStream(file: File): InputStreamReader {
        return InputStreamReader(fileToStream(file), StandardCharsets.UTF_8)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun fileToReadStream(inputStream: InputStream): InputStreamReader {
        return InputStreamReader(inputStream, StandardCharsets.UTF_8)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun fileToStream(file: File): FileInputStream {
        return FileInputStream(file)
    }
}