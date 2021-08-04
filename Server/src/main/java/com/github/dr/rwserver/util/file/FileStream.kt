package com.github.dr.rwserver.util.file

import com.github.dr.rwserver.func.VoidCons
import com.github.dr.rwserver.struct.Seq
import com.github.dr.rwserver.util.io.IoReadConversion
import com.github.dr.rwserver.util.log.Log
import java.io.IOException
import java.io.InputStream

internal class FileStream {
    companion object {
        fun readFileString(inputStream: InputStream): String {
            val result = StringBuilder()
            readFileData(inputStream) { e: String ->
                result.append(e).append("\r\n")
            }
            return result.toString()
        }

        fun readFileListString(inputStream: InputStream): Seq<String> {
            val result = Seq<String>()
            readFileData(inputStream) { value: String -> result.add(value) }
            return result
        }

        private fun readFileData(inputStream: InputStream, voidCons: VoidCons<String>) {
            try {
                IoReadConversion.streamBufferRead(inputStream).use { br ->
                    var line: String?
                    while (br.readLine().also { line = it } != null) {
                        voidCons[line]
                    }
                }
            } catch (e: IOException) {
                Log.error("[Read File] Error", e)
            }
        }
    }
}