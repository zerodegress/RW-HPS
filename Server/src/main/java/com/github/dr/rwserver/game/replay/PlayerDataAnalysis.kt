/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.game.replay

import com.github.dr.rwserver.game.replay.block.Block_1
import com.github.dr.rwserver.game.replay.block.PointF
import com.github.dr.rwserver.io.input.GameInputStream
import com.github.dr.rwserver.io.packet.Packet
import com.github.dr.rwserver.struct.Seq

class PlayerDataAnalysis(inStream: GameInputStream) {
    val playerIndex: Int
    val block_1:Block_1?

    val boolean_1: Boolean
    val isCancel: Boolean

    // TODO
    val string_1: String

    val doNo: A

    val pointF: PointF?

    val boolean_2: Boolean

    /** 批量执行的单位数量 */
    val batchCount: Int
    val batchCountList = Seq<Long>()

    val byte_1: Byte

    val ping: PointF?

    // TODO
    val string_2: String

    val boolean_3: Boolean

    val short_1: Short


    init {
        with (inStream) {
            playerIndex = readInt()
            block_1 = if (readBoolean()) Block_1(inStream) else null

            boolean_1 = readBoolean()
            isCancel = readBoolean()

            string_1 = readInt().toString()

            doNo = readEnum(A::class.java) as A

            pointF = if (readBoolean()) PointF(readFloat(),readFloat()) else null

            boolean_2 = readBoolean()

            batchCount = readInt()
            for (index in 0..batchCount) {
                batchCountList.add(readLong())
            }

            byte_1 = if (readBoolean()) readByte().toByte() else -1

            ping = if (readBoolean()) PointF(readFloat(),readFloat()) else null


            readLong() // False TODO

            string_2 = readString()

            boolean_3 = readBoolean()
            short_1 = readShort()
        }
    }

    enum class A {
        outOfRange, onlyInRange, returnFire, holdFire, guardArea, aggressive, mixed
    }

    companion object {
        val fastGameObjectList = Seq<PlayerDataAnalysis>()

        fun handlingTickPackages(packet: Packet) {
            GameInputStream(packet).use {
                val frameGame = it.readInt()/10
                val readCount = it.readInt()
                PlayerDataAnalysis(it.getDecodeStream(false))
            }
        }
    }
}