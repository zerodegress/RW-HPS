package net.rwhps.server.util.compression

import net.rwhps.server.io.GameOutputStream
import net.rwhps.server.io.input.DisableSyncByteArrayInputStream
import net.rwhps.server.util.ExtractUtil
import net.rwhps.server.util.log.Log
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream

/**
 * @date  2023/5/27 11:40
 * @author  RW-HPS/Dr
 */
class CompressionEncoderUtilsTest {
    init {
        Log.set("ERROR")
        Log.setCopyPrint(true)
    }

    @Test
    fun zipStream() {
        ExtractUtil.tryRunTest {
            val zip = CompressionEncoderUtils.zipStream()
            val bytes = GameOutputStream().apply {
                writeInt(0)
                writeString("Hi")
            }.getByteArray()

            zip.addCompressBytes("bytes", bytes)
            zip.addCompressBytes("inStream-HPS", DisableSyncByteArrayInputStream(bytes))
            zip.addCompressBytes("inStream-Java", ByteArrayInputStream(bytes))
            zip.addCompressBytes("/bytes/bytes", bytes)
            zip.addCompressBytes("/bytes/inStream-HPS", DisableSyncByteArrayInputStream(bytes))
            zip.addCompressBytes("/bytes/inStream-Java", ByteArrayInputStream(bytes))

            val zipNew = CompressionEncoderUtils.zipStream()
            zipNew.addCompressBytes("bytes_New", bytes)
            zipNew.addCompressBytes("inStream-HPS_New", DisableSyncByteArrayInputStream(bytes))
            zipNew.addCompressBytes("inStream-Java_New", ByteArrayInputStream(bytes))
            zipNew.addCompressBytes("/bytes/bytes", bytes)
            zipNew.addCompressBytes("/bytes/inStream-HPS", DisableSyncByteArrayInputStream(bytes))
            zipNew.addCompressBytes("/bytes/inStream-Java", ByteArrayInputStream(bytes))

            val unzip = CompressionDecoderUtils.zipStream(DisableSyncByteArrayInputStream(zipNew.flash()))

            zip.addCompressBytes("zip_New", unzip)

//            val file = FileUtil.getFile("CompressionEncoderUtilsTest.zip")
//            file.writeFileByte(zip.flash())
        }
    }

    @Test
    fun lz77Stream() {
        ExtractUtil.tryRunTest {
            val lz77 = CompressionEncoderUtils.lz77Stream()
            val bytes = GameOutputStream().apply {
                writeInt(0)
                writeString("Hi")
            }.getByteArray()

            lz77.addCompressBytes("bytes", bytes)
            lz77.addCompressBytes("inStream-HPS", DisableSyncByteArrayInputStream(bytes))
            lz77.addCompressBytes("inStream-Java", ByteArrayInputStream(bytes))
            lz77.addCompressBytes("/bytes/bytes", bytes)
            lz77.addCompressBytes("/bytes/inStream-HPS", DisableSyncByteArrayInputStream(bytes))
            lz77.addCompressBytes("/bytes/inStream-Java", ByteArrayInputStream(bytes))

            val lz77New = CompressionEncoderUtils.lz77Stream()
            lz77New.addCompressBytes("bytes_New", bytes)
            lz77New.addCompressBytes("inStream-HPS_New", DisableSyncByteArrayInputStream(bytes))
            lz77New.addCompressBytes("inStream-Java_New", ByteArrayInputStream(bytes))
            lz77New.addCompressBytes("/bytes/bytes", bytes)
            lz77New.addCompressBytes("/bytes/inStream-HPS", DisableSyncByteArrayInputStream(bytes))
            lz77New.addCompressBytes("/bytes/inStream-Java", ByteArrayInputStream(bytes))

            val unzip = CompressionDecoderUtils.lz77Stream(DisableSyncByteArrayInputStream(lz77New.flash()))

            lz77.addCompressBytes("7z_New", unzip)

//            val file = FileUtil.getFile("CompressionEncoderUtilsTest.7z")
//            file.writeFileByte(lz77.flash())
        }
    }
}