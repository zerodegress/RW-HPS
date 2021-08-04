package com.github.dr.rwserver.util.zip.gzip

import com.github.dr.rwserver.util.IsUtil
import com.github.dr.rwserver.util.log.Log.error
import java.io.*
import java.util.zip.GZIPOutputStream

/**
 * @author Dr
 */
class GzipEncoder(bl: Boolean) : Closeable {
    var str: String? = null

    @JvmField
    val buffer: ByteArrayOutputStream = ByteArrayOutputStream()

    lateinit var stream: DataOutputStream

    private var gzip: GZIPOutputStream? = null

    init {
        if (bl) {
            try {
                gzip = GZIPOutputStream(buffer)
                stream = DataOutputStream(BufferedOutputStream(gzip as OutputStream))
            } catch (e: IOException) {
                error("GZIP Error", e)
            }
        } else {
            stream = DataOutputStream(buffer)
        }
    }

    override fun close() {
        try {
            stream.flush()
            buffer.flush()
            if (IsUtil.notIsBlank(gzip)) {
                gzip!!.close()
            }
        } catch (e: Exception) {
            error("Close Gzip", e)
        }
    }

    companion object {
        @JvmStatic
        @Throws(Exception::class)
        fun getGzipOutputStream(out: OutputStream?): OutputStream {
            return BufferedOutputStream(GZIPOutputStream(out))
        }

        @JvmStatic
        fun getGzipStream(key: String?, bl: Boolean): GzipEncoder {
            val enc = GzipEncoder(bl)
            enc.str = key
            return enc
        }
    }
}