/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.io.packet

import net.rwhps.server.data.global.Data
import net.rwhps.server.data.global.NetStaticData
import net.rwhps.server.io.GameInputStream
import net.rwhps.server.io.inandout.GameInputStreamAndOutputStream
import net.rwhps.server.util.ExtractUtil
import net.rwhps.server.util.PacketType
import net.rwhps.server.util.log.Log

class GameSavePacket(val packet: Packet) {
    fun convertGameSaveDataPacket(): Packet {
        return NetStaticData.RwHps.abstractNetPacket.convertGameSaveDataPacket(packet)
    }

    /**
     * 检测 诱骗的GameSave包是否对得住 (防止获取到的是旧的)
     * @return Boolean
     */
    fun checkTick(): Boolean {
        GameInputStream(packet).use { stream ->
            stream.readByte()
            val tick = stream.readInt()
            val tickGame = Data.game.tickGame.get()
            return ((tickGame - 50) < tick && tick < (tickGame + 50))
        }
    }

    /**
     * 解析 GameSave包
     */
    fun analyze() {
        GameInputStream(packet,151).use { stream ->
            stream.readByte()
            Log.clog("Tick : {0}",stream.readInt())
            Log.clog("{0}",stream.readInt())
            Log.clog("{0}",stream.readFloat())
            Log.clog("{0}",stream.readFloat())
            Log.clog("{0}",stream.readBoolean())
            Log.clog("{0}",stream.readBoolean())

            stream.getDecodeStream(false).use { gameSave ->
                Log.clog(gameSave.readString())
                gameSave.readInt()

                val gameSaveVersion: Int
                Log.clog("GameSave Version: {0}", gameSave.readInt().also { gameSaveVersion = it })
                gameSave.readBoolean()
                if (gameSaveVersion >= 23) {
                    gameSave.getDecodeStream(true).use { saveCompression ->
                        if (gameSaveVersion >= 54) {
                            saveCompression.getDecodeStream(false).use { customUnitsBlock ->
                                // 我 不 知 道
                                Log.clog("customUnitsBlock SKIP !  -> {0}", customUnitsBlock.getSize())
                            }
                        }
                        if (gameSaveVersion >= 56) {
                            saveCompression.getDecodeStream(false).use { gameSetup ->
                                if (gameSetup.readBoolean()) {
                                    gameSetup.readByte()

                                    // version
                                    gameSetup.readByte()
                                    Log.clog("fogMode : {0}", gameSetup.readInt())
                                    Log.clog("startingCredits : {0}", gameSetup.readInt())
                                    Log.clog("revealedMap : {0}", gameSetup.readBoolean())
                                    Log.clog("aiDifficulty : {0}", gameSetup.readInt())
                                    Log.clog("startingUnits : {0}", gameSetup.readInt())
                                    Log.clog("incomeMultiplier : {0}", gameSetup.readFloat())
                                    Log.clog("noNukes : {0}", gameSetup.readBoolean())
                                    Log.clog("? Boolean : {0}", gameSetup.readBoolean())
                                    Log.clog("sharedControl : {0}", gameSetup.readBoolean())
                                    // Version 1
                                    Log.clog("? Boolean : {0}", gameSetup.readBoolean())
                                    // Version 2
                                    Log.clog("? Boolean : {0}", gameSetup.readBoolean())
                                    // Version 3
                                    Log.clog("allowSpectators : {0}", gameSetup.readBoolean())
                                    Log.clog("lockedRoom : {0}", gameSetup.readBoolean())

                                    Log.clog("MaxUnit : {0}", gameSetup.readInt())
                                    Log.clog("MaxUnit : {0}", gameSetup.readInt())
                                }
                            }
                        }

                        Log.clog("MapName ? : {0}", saveCompression.readString())

                        if (gameSaveVersion >= 72) {
                            if (saveCompression.readBoolean()) {
                                val mapBytes = saveCompression.readStreamBytes()
                                Log.clog("Reading remote map stream : Size: {0}", mapBytes.size)
                            }
                        }

                        Log.clog("FirstActivation: move at : {0}", saveCompression.readInt())
                        Log.clog("? Float : {0}", saveCompression.readFloat())
                        Log.clog("? Float : {0}", saveCompression.readFloat())
                        Log.clog("? Float : {0}", saveCompression.readFloat())


                        if (gameSaveVersion >= 18) {
                            Log.clog("? Int : {0}", saveCompression.readInt())
                        }
                        saveCompression.readInt() // 0

                        if (gameSaveVersion >= 19) {
                            // 没用找到标记位置 (读取未到达置顶位置)
                            if (saveCompression.readShort() != "12345".toShort()) {
                                Log.clog("Mark wasn't read for: End of Setup")
                            }
                        }

                        if (saveCompression.readBoolean()) {
                            val a = saveCompression.readInt()
                            val b = saveCompression.readInt()
                            for (i2 in 0 until a) {
                                for (i3 in 0 until b) {
                                    saveCompression.readByte()
                                }
                            }
                        }

                        if (gameSaveVersion >= 86) {
                            saveCompression.readBoolean()
                            saveCompression.readBoolean()
                            saveCompression.readBoolean()
                            saveCompression.readBoolean()
                        }

                        // MissionEngine Sync
                        if (saveCompression.readBoolean()) {
                            // But i don't want to write MissionEngine
                            Log.clog("? Boolean : {0}", saveCompression.readBoolean())
                            Log.clog("? Int : {0}", saveCompression.readInt())
                            Log.clog("? Int : {0}", saveCompression.readInt())
                            Log.clog("? Int : {0}", saveCompression.readInt())
                            Log.clog("? Int : {0}", saveCompression.readInt())
                            Log.clog("? Int : {0}", saveCompression.readInt())
                            Log.clog("? Float : {0}", saveCompression.readFloat())
                            Log.clog("? Float : {0}", saveCompression.readFloat())
                            Log.clog("? Float : {0}", saveCompression.readFloat())
                            Log.clog("? Boolean : {0}", saveCompression.readBoolean())

                            val missionEngineVersion: Int
                            Log.clog("MissionEngine Version : {0}", saveCompression.readInt().also { missionEngineVersion = it })

                            if (missionEngineVersion >= 1) {
                                val count = saveCompression.readInt()
                                for ( i in 0 until count) {
                                    val missionEngine = saveCompression.readString()
                                    Log.clog("? Boolean : {0}", saveCompression.readBoolean())

                                    if (missionEngineVersion >= 2) {
                                        Log.clog("? Int : {0}", saveCompression.readInt())
                                        Log.clog("? Int : {0}", saveCompression.readInt())
                                    }
                                    if (missionEngineVersion >= 3) {
                                        Log.clog("? Boolean : {0}", saveCompression.readBoolean())
                                    }
                                    if (missionEngineVersion >= 4) {
                                        Log.clog("? Int : {0}", saveCompression.readInt())
                                    }

                                }
                            }
                            if (missionEngineVersion >= 5) {
                                Log.clog("? Int : {0}", saveCompression.readInt())
                            }
                            if (missionEngineVersion >= 6) {
                                Log.clog("? Boolean : {0}", saveCompression.readBoolean())
                            }
                        }

                        if (gameSaveVersion >= 19) {
                            // 没用找到标记位置 (读取未到达置顶位置)
                            if (saveCompression.readShort() != "12345".toShort()) {
                                Log.clog("Mark wasn't read for: Start of teams")
                            }
                        }

                        if (gameSaveVersion >= 36) {
                            Log.clog("? Int : {0}", saveCompression.readInt())
                        }

                        var maxPlayer = 8
                        if (gameSaveVersion >= 49) {
                            Log.clog("maxPlayer : {0}", saveCompression.readInt().also { maxPlayer = it })
                            // Clear Player Array
                            /*
                            m.b(i2, false);
                            for (int i3 = 0; i3 < m.f153a; i3++) {
                                if (i3 >= i2 && !z && (n = m.n(i3)) != null) {
                                    n.E();
                                }
                            }*/
                        }

                        for (i in 0 until maxPlayer) {
                            val isAi = saveCompression.readBoolean()
                            val isReplacing: Boolean
                            if (gameSaveVersion >= 7) {
                                isReplacing = saveCompression.readBoolean()
                            }

                            if (saveCompression.readBoolean()) {
                                Log.clog("A")
                                if (gameSaveVersion >= 2) {
                                    val site = saveCompression.readByte()

                                    saveCompression.skip(8)// Int+Int
                                    Log.clog(saveCompression.readIsString())
                                    saveCompression.skip(1)
                                    saveCompression.skip(12) // Int+Long
                                    saveCompression.skip(5)  // Boolean+Int
                                    saveCompression.skip(5)  // Int+Byte
                                    saveCompression.skip(2)  // Boolean *2
                                    saveCompression.skip(6) // Boolean+Boolean+Int
                                    saveCompression.readIsString(); saveCompression.skip(4)
                                }
                            } else {
                                Log.clog("B")
                            }
                        }

                        // not finished continue ....
                        Log.clog("saveCompression SKIP !  -> {0}", saveCompression.getSize())
                    }
                }
                Log.clog("gameSave SKIP !  -> {0}",gameSave.getSize())
            }
        }
    }

    /**
     * 解析 GameSave包 并生成作弊包
     */
    fun cheatSync(): Packet {
        Log.clog("check ${packet.bytes.size}")
        GameInputStreamAndOutputStream(packet,151).use { stream ->
            stream.readByteN()
            stream.readIntN()
            stream.readIntN()
            stream.readFloatN()
            stream.readFloatN()
            stream.readBooleanN()
            stream.readBooleanN()

            stream.getDecodeStreamNoData(false).use { gameSave ->
                gameSave.readStringN()
                gameSave.readIntN()

                val gameSaveVersion: Int
                Log.clog("GameSave Version: {0}", gameSave.readInt().also { gameSaveVersion = it })
                gameSave.readBooleanN()
                if (gameSaveVersion >= 23) {
                    gameSave.getDecodeStreamNoData(true).use { saveCompression ->
                        if (gameSaveVersion >= 54) {
                            saveCompression.getDecodeStreamN(false)
                        }
                        if (gameSaveVersion >= 56) {
                            // gameSetup
                            saveCompression.getDecodeStreamN(false)
                        }

                        Log.clog("MapName ? : {0}", saveCompression.readString())

                        if (gameSaveVersion >= 72) {
                            if (saveCompression.readBoolean()) {
                                saveCompression.readStreamBytesN()
                            }
                        }

                        saveCompression.readIntN()
                        saveCompression.readFloatN()
                        saveCompression.readFloatN()
                        saveCompression.readFloatN()


                        if (gameSaveVersion >= 18) {
                            saveCompression.readIntN()
                        }
                        saveCompression.readIntN() // 0

                        if (gameSaveVersion >= 19) {
                            // 没用找到标记位置 (读取未到达置顶位置)
                            if (saveCompression.readShort() != "12345".toShort()) {
                                Log.clog("Mark wasn't read for: End of Setup")
                            }
                        }

                        if (saveCompression.readBoolean()) {
                            val a = saveCompression.readInt()
                            val b = saveCompression.readInt()
                            for (i2 in 0 until a) {
                                for (i3 in 0 until b) {
                                    saveCompression.readByte()
                                }
                            }
                        }

                        if (gameSaveVersion >= 86) {
                            saveCompression.readBoolean()
                            saveCompression.readBoolean()
                            saveCompression.readBoolean()
                            saveCompression.readBoolean()
                        }

                        // MissionEngine Sync
                        if (saveCompression.readBoolean()) {
                            // But i don't want to write MissionEngine
                            Log.clog("? Boolean : {0}", saveCompression.readBoolean())
                            Log.clog("? Int : {0}", saveCompression.readInt())
                            Log.clog("? Int : {0}", saveCompression.readInt())
                            Log.clog("? Int : {0}", saveCompression.readInt())
                            Log.clog("? Int : {0}", saveCompression.readInt())
                            Log.clog("? Int : {0}", saveCompression.readInt())
                            Log.clog("? Float : {0}", saveCompression.readFloat())
                            Log.clog("? Float : {0}", saveCompression.readFloat())
                            Log.clog("? Float : {0}", saveCompression.readFloat())
                            Log.clog("? Boolean : {0}", saveCompression.readBoolean())

                            val missionEngineVersion: Int
                            Log.clog("MissionEngine Version : {0}", saveCompression.readInt().also { missionEngineVersion = it })

                            if (missionEngineVersion >= 1) {
                                val count = saveCompression.readInt()
                                for ( i in 0 until count) {
                                    val missionEngine = saveCompression.readString()
                                    Log.clog("? Boolean : {0}", saveCompression.readBoolean())

                                    if (missionEngineVersion >= 2) {
                                        Log.clog("? Int : {0}", saveCompression.readInt())
                                        Log.clog("? Int : {0}", saveCompression.readInt())
                                    }
                                    if (missionEngineVersion >= 3) {
                                        Log.clog("? Boolean : {0}", saveCompression.readBoolean())
                                    }
                                    if (missionEngineVersion >= 4) {
                                        Log.clog("? Int : {0}", saveCompression.readInt())
                                    }

                                }
                            }
                            if (missionEngineVersion >= 5) {
                                Log.clog("? Int : {0}", saveCompression.readInt())
                            }
                            if (missionEngineVersion >= 6) {
                                Log.clog("? Boolean : {0}", saveCompression.readBoolean())
                            }
                        }

                        if (gameSaveVersion >= 19) {
                            // 没用找到标记位置 (读取未到达置顶位置)
                            if (saveCompression.readShort() != "12345".toShort()) {
                                Log.clog("Mark wasn't read for: Start of teams")
                            }
                        }

                        if (gameSaveVersion >= 36) {
                            Log.clog("? Int : {0}", saveCompression.readInt())
                        }

                        var maxPlayer = 8
                        if (gameSaveVersion >= 49) {
                            Log.clog("maxPlayer : {0}", saveCompression.readInt().also { maxPlayer = it })
                        }

                        for (i in 0 until maxPlayer) {
                            val isAi = saveCompression.readBoolean()
                            val isReplacing: Boolean
                            if (gameSaveVersion >= 7) {
                                isReplacing = saveCompression.readBoolean()
                            }

                            if (saveCompression.readBoolean()) {
                                Log.clog("A")
                                if (gameSaveVersion >= 2) {
                                    val site = saveCompression.readByte()
                                    saveCompression.skip(4)
                                    saveCompression.out.writeInt(100000)
                                    saveCompression.skipN(4)// Int+Int
                                    saveCompression.readIsStringN()
                                    saveCompression.skipN(1)
                                    saveCompression.skipN(12) // Int+Long
                                    saveCompression.skipN(5)  // Boolean+Int
                                    saveCompression.skipN(5)  // Int+Byte
                                    saveCompression.skipN(2)  // Boolean *2
                                    saveCompression.skipN(6) // Boolean+Boolean+Int
                                    saveCompression.readIsStringN(); saveCompression.skipN(4)
                                }
                            } else {
                                Log.clog("B")
                            }
                        }

                        // not finished continue ....
                        saveCompression.out.writeBytes(saveCompression.readAllBytes())
                        gameSave.transferTo(saveCompression,true)
                    }
                }

                gameSave.out.writeBytes(gameSave.readAllBytes())
                stream.transferTo(gameSave)
            }

            return stream.out.createPacket(PacketType.SYNC)
        }
    }

    /**
     * Return detailed Packet data
     * @return Packet String
     */
    override fun toString(): String {
        return  """
                GameSavePacket {
                    Bytes=${packet.bytes.contentToString()}
                    BytesHex=${ExtractUtil.bytesToHex(packet.bytes)}
                    Type=${packet.type}
                }
                """.trimIndent()
    }
}