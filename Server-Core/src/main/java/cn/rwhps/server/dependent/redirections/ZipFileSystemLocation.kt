package cn.rwhps.server.dependent.redirections

import cn.rwhps.server.io.input.DisableSyncByteArrayInputStream
import cn.rwhps.server.util.file.FileUtil
import cn.rwhps.server.util.zip.zip.ZipDecoder
import org.newdawn.slick.util.ResourceLocation
import java.io.InputStream
import java.net.URL

class ZipFileSystemLocation(private val zipDecoder: ZipDecoder) : ResourceLocation {
    val a = zipDecoder.getZipAllBytes()
    override fun getResourceAsStream(ref: String): InputStream? {
        return a.get(ref)?.let { DisableSyncByteArrayInputStream(it) }
    }

    override fun getResource(ref: String?): URL {
        return FileUtil.getFile("a.ogg").file.toURI().toURL()
    }
}