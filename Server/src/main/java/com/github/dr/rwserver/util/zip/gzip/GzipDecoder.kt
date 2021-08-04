package com.github.dr.rwserver.util.zip.gzip

import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.InputStream
import java.util.zip.GZIPInputStream

/**
 * @author Dr
 */
class GzipDecoder(isGzip: Boolean, bytes: ByteArray) {
    val buffer: ByteArrayInputStream = ByteArrayInputStream(bytes)
    var stream: DataInputStream

    init {
        stream =  if (isGzip) DataInputStream(BufferedInputStream(GZIPInputStream(buffer))) else DataInputStream(buffer)
    }

    companion object {
        @JvmStatic
		@Throws(Exception::class)
        fun getGzipInputStream(inputStream: InputStream): InputStream {
            return BufferedInputStream(GZIPInputStream(inputStream))
        }
    }
}