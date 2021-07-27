package com.github.dr.rwserver.util.file

import com.github.dr.rwserver.func.VoidCons
import com.github.dr.rwserver.struct.Seq
import com.github.dr.rwserver.util.IsUtil
import com.github.dr.rwserver.util.io.IoOutConversion.fileToOutStream
import com.github.dr.rwserver.util.io.IoOutConversion.fileToStream
import com.github.dr.rwserver.util.io.IoRead.readFileToByteArray
import com.github.dr.rwserver.util.io.IoReadConversion.fileToReadStream
import com.github.dr.rwserver.util.io.IoReadConversion.fileToStream
import com.github.dr.rwserver.util.io.IoReadConversion.streamBufferRead
import com.github.dr.rwserver.util.log.Log
import com.github.dr.rwserver.util.log.Log.error
import java.io.*

/**
 * @author Dr
 */
class FileUtil {
    /** 内部的File  */
    val file: File

    /** 当前操作的文件  */
    val path: String

    constructor(file: File) {
        this.file = file
        this.path = file.path
        mkdir()
    }

    constructor(filepath: String) {
        this.path = filepath
        file = File(filepath)
        mkdir()
    }

    private constructor(file: File, filepath: String) {
        this.file = file
        this.path = filepath
        mkdir()
    }

    private constructor(file: File, filepath: String, a: String) {
        this.file = file
        this.path = filepath
        file.mkdirs()
    }

    fun notExists(): Boolean {
        return !file.exists()
    }

    fun length(): Long {
        return file.length()
    }

    fun toPath(filename: String): FileUtil {
        //info(this.path + "/" + filename)
        return FileUtil(File(this.path + "/" + filename))
    }

    val fileList: Seq<File>
        get() {
            val array = file.listFiles()
            val fileList = Seq<File>()
            if (IsUtil.isBlank(array)) {
                return fileList
            }
            for (value in array!!) {
                if (!value.isDirectory) {
                    if (value.isFile) {
                        fileList.add(value)
                    }
                }
            }
            return fileList
        }
    val fileListNotNullSize: Seq<File>
        get() {
            val list = Seq<File>()
            fileList.eachBooleanIfs({ e: File -> e.length() > 0 }) { value: File -> list.add(value) }
            return list
        }

    /**
     *
     * @param log Log
     * @param cover 是否尾部写入
     */
    fun writeFile(log: Any, cover: Boolean) {
        try {
            fileToOutStream(file, cover).use { osw ->
                osw.write(log.toString())
                osw.flush()
            }
        } catch (e: Exception) {
            error("writeFile", e)
        }
    }

    fun writeFileByte(bytes: ByteArray, cover: Boolean) {
        try {
            BufferedOutputStream(FileOutputStream(file, cover)).use { osw ->
                osw.write(bytes)
                osw.flush()
            }
        } catch (e: Exception) {
            error("writeByteFile", e)
        }
    }

    @Throws(Exception::class)
    fun writeByteOutputStream(cover: Boolean): OutputStream {
        return fileToStream(file, cover)
    }

    @get:Throws(IOException::class)
    val inputsStream: FileInputStream
        get() = fileToStream(file)

    @Throws(IOException::class)
    fun readInputsStream(): InputStreamReader {
        return fileToReadStream(file)
    }

    @Throws(Exception::class)
    fun readFileByte(): ByteArray {
        return readFileToByteArray(file)
    }

    fun readFileStringData(): String {
        try {
            FileInputStream(file).use { fileInputStream -> return readFileString(fileInputStream) }
        } catch (fileNotFoundException: FileNotFoundException) {
            error("FileNotFoundException",fileNotFoundException)
        } catch (ioException: IOException) {
            error("Read IO Error", ioException)
        }
        return ""
    }

    fun readFileListStringData(): Seq<String> {
        try {
            FileInputStream(file).use { fileInputStream -> return readFileListString(fileInputStream) }
        } catch (fileNotFoundException: FileNotFoundException) {
            error("FileNotFoundException")
        } catch (ioException: IOException) {
            error("Read IO Error", ioException)
        }
        return Seq()
    }

    private fun mkdir() {
        file.parentFile.mkdirs()
        if (!file.exists()) {
            try {
                file.createNewFile()
            } catch (e: IOException) {
                Log.error("Mk file", e)
            }
        }
    }

    companion object {
        /**
         * 默认的地址前缀
         * 如果不为null将会直接使用path而不是使用jar的位置
         */
		private var defaultFilePath: String

        /**
         * 加载默认的文件路径
         * 默认使用Jar同目录下的 与启动的位置无关
         */
		init {
            val path = this.javaClass.protectionDomain.codeSource.location.path
            val pathSplit = path.split("/").toTypedArray()
            val jarName = pathSplit[pathSplit.size - 1]
            val jarPath = path.replace(jarName, "")

            defaultFilePath = jarPath
		}

        /**
         * 设置Jar的数据存储位置
         * @param customFilePath String
         */
        @JvmStatic
        fun setFilePath(customFilePath: String? = null) {
            if (customFilePath != null) {
                defaultFilePath = customFilePath
            }
        }

        @JvmStatic
		@JvmOverloads
        fun toFolder(toFile: String? = null): FileUtil {
            val filepath: String
            var to = toFile
            if (null != toFile) {
                /*
                 * 防止传入/开头的导致filepath//xxx
                 */
                if ("/" == toFile[0].toString()) {
                    to = toFile.substring(1,toFile.length)
                }
            }
            /* 防止传入/ */
            filepath = if (null == to) {
                defaultFilePath
            } else {
                defaultFilePath + to
            }
            return FileUtil(File(filepath), filepath,"")
        }

        @JvmStatic
        fun readFileString(inputStream: InputStream): String {
            val result = StringBuilder()
            readFileData(inputStream) { e: String ->
                result.append(e).append("\r\n")
            }
            return result.toString()
        }

        @JvmStatic
		fun readFileListString(inputStream: InputStream): Seq<String> {
            val result = Seq<String>()
            readFileData(inputStream) { value: String -> result.add(value) }
            return result
        }

        private fun readFileData(inputStream: InputStream, voidCons: VoidCons<String>) {
            try {
                streamBufferRead(inputStream).use { br ->
                    var line: String?
                    while (br.readLine().also { line = it } != null) {
                        voidCons[line]
                    }
                }
            } catch (e: IOException) {
                error("[Read File] Error", e)
            }
        }
    }
}