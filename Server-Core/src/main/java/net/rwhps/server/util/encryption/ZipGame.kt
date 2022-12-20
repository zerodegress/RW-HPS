/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.util.encryption

import net.rwhps.server.io.GameInputStream
import net.rwhps.server.io.GameOutputStream
import net.rwhps.server.util.ExtractUtil
import net.rwhps.server.util.ExtractUtil.hexToByteArray
import net.rwhps.server.util.file.FileUtil
import net.rwhps.server.util.log.Log

class ZipGame {
    fun jm() {
        val o = GameOutputStream()
        GameInputStream(FileUtil.getFile("Encrypt.Zip").readFileByte()).use {
            var flag: Zip = Zip.NULL
            while (it.getSize() > 0) {
                val byte: Byte = it.readByte().toByte()
                o.writeByte(byte)
                // Read ZIP File Head
                // 暴力匹配ZIP的中心目录区
                if (byte == Zip.A.byte && flag == Zip.NULL) {
                    flag = Zip.A
                } else if (byte == Zip.B.byte && flag == Zip.A) {
                    flag = Zip.B
                } else if (byte == Zip.HA.byte && flag == Zip.B) {
                    flag = Zip.HA
                } else if (byte == Zip.HB.byte && flag == Zip.HA) {
                    flag = Zip.HB
                    // 头部偏移 24 使其直接读取长度
                    // 不知道为什么 这里的只能读23
                    o.transferToFixedLength(it,22)
                    val fileNameLength = it.readBackwardsShort()
                    //val fileNameExLength = it.readShort()
                    Log.clog(fileNameLength.toString())

                    //out.writeShort(fileNameExLength)
                    // 偏移 16
                    // 不知道为什么 上图的24 补到了这
                    val bytesB = it.readNBytes(2)


                    val bytesName = it.readNBytes(fileNameLength.toInt()-1)
                    val bytesEnd = it.readByte()
                    if (bytesEnd.toByte() == "/"[0].code.toByte()) {
                        o.writeBackwardsShort(fileNameLength)
                        o.writeBytes(bytesB)
                        o.writeBytes(bytesName)
                        o.writeByte(bytesEnd)
                    } else {
                        if (ExtractUtil.bytesToHex(bytesName).replace(" ","").contains("2E 6E 6F 6D 65 64 69".replace(" ",""), true) ||
                            ExtractUtil.bytesToHex(bytesName).replace(" ","").contains("6D 6F 64 2D 69 6E 66 6F 2E 74 78".replace(" ",""), true)) {
                            flag = Zip.NULL
                            o.writeBackwardsShort(fileNameLength)
                            o.writeBytes(bytesB)
                            o.writeBytes(bytesName)
                            o.writeByte(bytesEnd)

                        } else {
                            if (bytesEnd.toByte() == "g"[0].code.toByte()) {
                                o.writeBackwardsShort((fileNameLength+4).toShort())
                                o.writeBytes(bytesB)
                                o.writeBytes(bytesName)
                                o.writeByte(bytesEnd)
                                o.writeByte("/"[0].code.toByte())
                                it.readNBytes(5)
                            } else {
                                o.writeBackwardsShort((fileNameLength+1).toShort())
                                o.writeBytes(bytesB)
                                o.writeBytes(bytesName)
                                o.writeByte(bytesEnd)
                                o.writeByte("/"[0].code.toByte())
                               // i++
                            }
                        }
                    }

                    flag = Zip.NULL
                } else {
                    flag = Zip.NULL
                }
            }
        }


        val out = GameOutputStream()
        var i = 0
        GameInputStream(o.getByteArray()).use {
            var flag: Zip = Zip.NULL
            while (it.getSize() > 0) {
                val byte: Byte = it.readByte().toByte()
                out.writeByte(byte)
                // Read ZIP File Head
                // 暴力匹配ZIP的中心目录区
                if (byte == Zip.A.byte && flag == Zip.NULL) {
                    flag = Zip.A
                    Log.clog(flag.toString())
                    Log.clog(it.getSize().toString())
                } else if (byte == Zip.B.byte && flag == Zip.A) {
                    flag = Zip.B
                    Log.clog(flag.toString())
                    Log.clog(it.getSize().toString())
                } else if (byte == Zip.C.byte && flag == Zip.B) {
                    flag = Zip.C
                    Log.clog(flag.toString())
                    Log.clog(it.getSize().toString())
                } else if (byte == Zip.D.byte && flag == Zip.C) {
                    flag = Zip.D
                    Log.clog(flag.toString())
                    Log.clog(it.getSize().toString())
                    // 头部偏移 24 使其直接读取长度
                    // 不知道为什么 这里的只能读23
                    out.transferToFixedLength(it,24)
                    val fileNameLength = it.readBackwardsShort()
                    //val fileNameExLength = it.readShort()
                    Log.clog(fileNameLength.toString())

                    //out.writeShort(fileNameExLength)
                    // 偏移 16
                    // 不知道为什么 上图的24 补到了这
                    val bytesB = it.readNBytes(16)


                    val bytesName = it.readNBytes(fileNameLength.toInt()-1)
                    val bytesEnd = it.readByte()
                    if (bytesEnd.toByte() == "/"[0].code.toByte()) {
                        out.writeBackwardsShort(fileNameLength)
                        out.writeBytes(bytesB)
                        out.writeBytes(bytesName)
                        out.writeByte(bytesEnd)
                    } else {
                        if (ExtractUtil.bytesToHex(bytesName).replace(" ","").contains("2E 6E 6F 6D 65 64 69".replace(" ",""), true) ||
                            ExtractUtil.bytesToHex(bytesName).replace(" ","").contains("6D 6F 64 2D 69 6E 66 6F 2E 74 78".replace(" ",""), true)) {
                            flag = Zip.NULL
                            out.writeBackwardsShort(fileNameLength)
                            out.writeBytes(bytesB)
                            out.writeBytes(bytesName)
                            out.writeByte(bytesEnd)

                        } else {

                            if (bytesEnd.toByte() == "g"[0].code.toByte()) {
                                out.writeBackwardsShort((fileNameLength+4).toShort())
                                out.writeBytes(bytesB)
                                out.writeBytes(bytesName)
                                out.writeByte(bytesEnd)
                                out.writeByte("/"[0].code.toByte())
                                i++
                            } else {
                                out.writeBackwardsShort((fileNameLength+1).toShort())
                                out.writeBytes(bytesB)
                                out.writeBytes(bytesName)
                                out.writeByte(bytesEnd)
                                out.writeByte("/"[0].code.toByte())
                                i++
                            }
                        }
                    }

                    flag = Zip.NULL
                } else {
                    flag = Zip.NULL
                }
            }
        }

        val outOut = GameOutputStream()
        GameInputStream(out.getByteArray()).use {
            var flag: Zip = Zip.NULL
            while (it.getSize() > 0) {
                val byte: Byte = it.readByte().toByte()
                outOut.writeByte(byte)
                // Read ZIP File Head
                // 暴力匹配ZIP的中心目录区

                if (byte == Zip.A.byte && flag == Zip.NULL) {
                    flag = Zip.A
                    Log.clog(flag.toString())
                    Log.clog(it.getSize().toString())
                } else if (byte == Zip.B.byte && flag == Zip.A) {
                    flag = Zip.B
                    Log.clog(flag.toString())
                    Log.clog(it.getSize().toString())
                } else if (byte == Zip.CKA.byte && flag == Zip.B) {
                    flag = Zip.CKA
                    Log.clog(flag.toString())
                    Log.clog(it.getSize().toString())
                } else if (byte == Zip.CKB.byte && flag == Zip.CKA) {
                    flag = Zip.CKB
                    Log.clog(flag.toString())
                    Log.clog(it.getSize().toString())
                    outOut.transferToFixedLength(it,8)

                    outOut.writeBackwardsInt(it.readBackwardsInt()+i)

                    flag = Zip.NULL
                } else {
                    flag = Zip.NULL
                }
            }

            FileUtil.getFile("Encrypt-OK.zip").writeFileByte(outOut.getByteArray(),false)
        }
    }

    fun um() {
        val out = GameOutputStream()
        GameInputStream(FileUtil.getFile("Decrypt.Zip").readFileByte()).use {
            var flag: Zip = Zip.NULL
            while (it.getSize() > 0) {
                val byte: Byte = it.readByte().toByte()
                out.writeByte(byte)
                // Read ZIP File Head
                // 暴力匹配ZIP的中心目录区
                if (byte == Zip.A.byte && flag == Zip.NULL) {
                    flag = Zip.A
                    Log.clog(flag.toString())
                    Log.clog(it.getSize().toString())
                } else if (byte == Zip.B.byte && flag == Zip.A) {
                    flag = Zip.B
                    Log.clog(flag.toString())
                    Log.clog(it.getSize().toString())
                } else if (byte == Zip.C.byte && flag == Zip.B) {
                    flag = Zip.C
                    Log.clog(flag.toString())
                    Log.clog(it.getSize().toString())
                } else if (byte == Zip.D.byte && flag == Zip.C) {
                    flag = Zip.D
                    Log.clog(flag.toString())
                    Log.clog(it.getSize().toString())
                    // 头部偏移 24 使其直接读取长度
                    // 不知道为什么 这里的只能读23
                    out.transferToFixedLength(it,23)
                    val fileNameLength = it.readShort()
                    //val fileNameExLength = it.readShort()
                    Log.clog(fileNameLength.toString())

                    val bytesB = it.readNBytes(17)
                    val bytesName = it.readNBytes(fileNameLength.toInt()-1)
                    val bytesEnd = it.readByte()
                    if (bytesEnd.toByte() == "/"[0].code.toByte()) {
                        out.writeShort((fileNameLength-1).toShort())
                        out.writeBytes(bytesB)
                        out.writeBytes(bytesName)
                    } else {
                        out.writeShort(fileNameLength)
                        out.writeBytes(bytesB)
                        out.writeBytes(bytesName)
                        out.writeByte(bytesEnd)
                    }

                    out.transferToFixedLength(it,fileNameLength.toInt())
                    flag = Zip.NULL
                } else {
                    flag = Zip.NULL
                }
            }
            FileUtil.getFile("Decrypt-OK.zip").writeFileByte(out.getByteArray(),false)
        }
    }

    //中心目录区 头部
    enum class Zip(val byte: Byte)  {
        A(hexToByteArray("50")[0]),
        B(hexToByteArray("4B")[0]),
        C(hexToByteArray("01")[0]),
        D(hexToByteArray("02")[0]),
        HA(hexToByteArray("03")[0]),
        HB(hexToByteArray("04")[0]),
        CKA(hexToByteArray("05")[0]),
        CKB(hexToByteArray("06")[0]),
        NULL(0);
    }
}