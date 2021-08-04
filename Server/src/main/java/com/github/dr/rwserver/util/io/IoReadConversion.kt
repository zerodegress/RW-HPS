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