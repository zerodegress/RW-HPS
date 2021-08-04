package com.github.dr.rwserver.util.io

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

object IoOutConversion {
    @JvmStatic
    @Throws(IOException::class)
    fun fileToOutStream(file: File, cover: Boolean): OutputStreamWriter {
        return OutputStreamWriter(fileToStream(file, cover), StandardCharsets.UTF_8)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun fileToStream(file: File, cover: Boolean): FileOutputStream {
        return FileOutputStream(file, cover)
    }
}