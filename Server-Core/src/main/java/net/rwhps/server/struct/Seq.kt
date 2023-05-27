/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.struct

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectLists
import net.rwhps.server.data.plugin.AbstractPluginData
import net.rwhps.server.data.plugin.DefaultSerializers
import net.rwhps.server.io.GameInputStream
import net.rwhps.server.io.GameOutputStream
import java.io.IOException

/**
 * 可调整大小，有序的对象数组
 *
 * 别骂了别骂了 找不到什么好方法
 * 获取同步后得到的就是 ObjectList 只能这样分着写
 * 我不想每次判断
 *
 * @param T
 * @property list ObjectList<T>
 * @property size Int
 *
 * @author RW-HPS/Dr
 */
class Seq<T>: BaseSeq<T> {
    @JvmOverloads constructor(threadSafety: Boolean = false): this(16, threadSafety)

    @Suppress("UNCHECKED_CAST", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    @JvmOverloads constructor(capacity: Int, threadSafety: Boolean = false): super(
        ObjectArrayList<T>(capacity).let {
            if (threadSafety) {
                ObjectLists.synchronize<T>(it, it)
            } else {
                it
            }
        } as java.util.List<T>
    )

    @Suppress("UNCHECKED_CAST", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    @JvmOverloads constructor(array: Array<T>, threadSafety: Boolean = false): super(
        ObjectArrayList<T>(array).let {
            if (threadSafety) {
                ObjectLists.synchronize<T>(it, it)
            } else {
                it
            }
        } as java.util.List<T>
    )

    companion object {
        /*
         * serializer
         */
        val serializer = object : SerializerTypeAll.TypeSerializer<Seq<*>> {
            @Throws(IOException::class)
            override fun write(paramDataOutput: GameOutputStream, objectParam: Seq<*>) {
                paramDataOutput.writeInt(objectParam.size)
                if (objectParam.size != 0) {
                    val first = objectParam.first()!!
                    val ser = AbstractPluginData.getSerializer(first.javaClass) ?: throw DefaultSerializers.getError(
                        first.javaClass.toString()
                    )
                    paramDataOutput.writeString(first.javaClass.name)
                    for (element in objectParam) {
                        ser.write(paramDataOutput, element)
                    }
                }
            }

            @Throws(IOException::class)
            override fun read(paramDataInput: GameInputStream): Seq<*> {
                val arr = Seq<Any?>()
                try {
                    val size = paramDataInput.readInt()
                    if (size == 0) {
                        return arr
                    }
                    val type = paramDataInput.readString()
                    val ser = AbstractPluginData.getSerializer(DefaultSerializers.lookup(type))
                        ?: throw DefaultSerializers.getError(type)

                    for (i in 0 until size) {
                        arr.add(ser.read(paramDataInput))
                    }
                    return arr
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                }
                return arr
            }
        }
    }
}