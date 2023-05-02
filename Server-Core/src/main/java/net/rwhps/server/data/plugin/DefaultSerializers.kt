/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.data.plugin

import net.rwhps.server.io.GameInputStream
import net.rwhps.server.io.GameOutputStream
import net.rwhps.server.io.packet.Packet
import net.rwhps.server.net.Administration.PlayerAdminInfo
import net.rwhps.server.net.Administration.PlayerInfo
import net.rwhps.server.struct.ObjectMap
import net.rwhps.server.struct.OrderedMap
import net.rwhps.server.struct.Seq
import net.rwhps.server.struct.SerializerTypeAll.TypeSerializer
import net.rwhps.server.util.algorithms.NetConnectProofOfWork
import java.io.IOException

/**
 * @author RW-HPS/Dr
 */
internal object DefaultSerializers {
    fun register() {
        registrationBasis()
        registerMap()
        registerCustomClass()
    }

    private fun registrationBasis() {
        AbstractPluginData.setSerializer(String::class.java, object : TypeSerializer<String?> {
            @Throws(IOException::class)
            override fun write(paramDataOutput: GameOutputStream, objectParam: String?) {
                paramDataOutput.writeString(objectParam ?: "")
            }

            @Throws(IOException::class)
            override fun read(paramDataInput: GameInputStream): String {
                return paramDataInput.readString()
            }
        })
        AbstractPluginData.setSerializer(Integer::class.java, object : TypeSerializer<Int> {
            @Throws(IOException::class)
            override fun write(paramDataOutput: GameOutputStream, objectParam: Int) {
                paramDataOutput.writeInt(objectParam)
            }

            @Throws(IOException::class)
            override fun read(paramDataInput: GameInputStream): Int {
                return paramDataInput.readInt()
            }
        })
        AbstractPluginData.setSerializer(java.lang.Float::class.java, object : TypeSerializer<Float> {
            @Throws(IOException::class)
            override fun write(paramDataOutput: GameOutputStream, objectParam: Float) {
                paramDataOutput.writeFloat(objectParam)
            }

            @Throws(IOException::class)
            override fun read(paramDataInput: GameInputStream): Float {
                return paramDataInput.readFloat()
            }
        })
        AbstractPluginData.setSerializer(java.lang.Long::class.java, object : TypeSerializer<Long> {
            @Throws(IOException::class)
            override fun write(paramDataOutput: GameOutputStream, objectParam: Long) {
                paramDataOutput.writeLong(objectParam)
            }

            @Throws(IOException::class)
            override fun read(paramDataInput: GameInputStream): Long {
                return paramDataInput.readLong()
            }
        })
        AbstractPluginData.setSerializer(java.lang.Boolean::class.java, object : TypeSerializer<Boolean> {
            @Throws(IOException::class)
            override fun write(paramDataOutput: GameOutputStream, objectParam: Boolean) {
                paramDataOutput.writeBoolean(objectParam)
            }

            @Throws(IOException::class)
            override fun read(paramDataInput: GameInputStream): Boolean {
                return paramDataInput.readBoolean()
            }
        })
        AbstractPluginData.setSerializer(ByteArray::class.java, object : TypeSerializer<ByteArray> {
            @Throws(IOException::class)
            override fun write(paramDataOutput: GameOutputStream, objectParam: ByteArray) {
                paramDataOutput.writeBytesAndLength(objectParam)
            }

            @Throws(IOException::class)
            override fun read(paramDataInput: GameInputStream): ByteArray {
                return paramDataInput.readStreamBytes()
            }
        })
    }

    private fun registerMap() {
        AbstractPluginData.setSerializer(Seq::class.java, Seq.serializer)

        AbstractPluginData.setSerializer(ObjectMap::class.java, object : TypeSerializer<ObjectMap<*, *>> {
            @Throws(IOException::class)
            override fun write(paramDataOutput: GameOutputStream, objectParam: ObjectMap<*, *>) {
                paramDataOutput.writeInt(objectParam.size)
                if (objectParam.size == 0) {
                    return
                }
                val entry = objectParam.entries().next()
                val keySer = AbstractPluginData.getSerializer(entry.key.javaClass) ?: throw getError(entry.key.javaClass.toString())
                val valSer = AbstractPluginData.getSerializer(entry.value.javaClass) ?: throw getError(entry.value.javaClass.toString())
                paramDataOutput.writeString(entry.key.javaClass.name)
                paramDataOutput.writeString(entry.value.javaClass.name)
                for (e in objectParam.entries()) {
                    val en = e as ObjectMap.Entry<*, *>
                    keySer.write(paramDataOutput, en.key)
                    valSer.write(paramDataOutput, en.value)
                }
            }

            @Throws(IOException::class)
            override fun read(paramDataInput: GameInputStream): ObjectMap<*, *> {
                val map = ObjectMap<Any?, Any?>()
                try {
                    val size = paramDataInput.readInt()
                    if (size == 0) {
                        return map
                    }
                    val keySerName = paramDataInput.readString()
                    val valSerName = paramDataInput.readString()
                    val keySer = AbstractPluginData.getSerializer(lookup(keySerName)) ?: throw getError(keySerName)
                    val valSer = AbstractPluginData.getSerializer(lookup(valSerName)) ?: throw getError(valSerName)
                    for (i in 0 until size) {
                        val key = keySer.read(paramDataInput)
                        val `val` = valSer.read(paramDataInput)
                        map.put(key, `val`)
                    }
                    return map
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                }
                return map
            }
        })
        AbstractPluginData.setSerializer(OrderedMap::class.java, object : TypeSerializer<OrderedMap<*, *>> {
            @Throws(IOException::class)
            override fun write(paramDataOutput: GameOutputStream, objectParam: OrderedMap<*, *>) {
                paramDataOutput.writeInt(objectParam.size)
                if (objectParam.size == 0) {
                    return
                }
                val entry = objectParam.entries().next()
                val keySer = AbstractPluginData.getSerializer(entry.key.javaClass) ?: throw getError(entry.key.javaClass.toString())
                val valSer = AbstractPluginData.getSerializer(entry.value.javaClass) ?: throw getError(entry.value.javaClass.toString())
                paramDataOutput.writeString(entry.key.javaClass.name)
                paramDataOutput.writeString(entry.value.javaClass.name)
                for (e in objectParam.entries()) {
                    val en = e as ObjectMap.Entry<*, *>
                    keySer.write(paramDataOutput, en.key)
                    valSer.write(paramDataOutput, en.value)
                }
            }

            @Throws(IOException::class)
            override fun read(paramDataInput: GameInputStream): OrderedMap<*, *> {
                val map = OrderedMap<Any?, Any?>()
                try {
                    val size = paramDataInput.readInt()
                    if (size == 0) {
                        return map
                    }
                    val keySerName = paramDataInput.readString()
                    val valSerName = paramDataInput.readString()
                    val keySer = AbstractPluginData.getSerializer(lookup(keySerName)) ?: throw getError(keySerName)
                    val valSer = AbstractPluginData.getSerializer(lookup(valSerName)) ?: throw getError(valSerName)
                    for (i in 0 until size) {
                        val key = keySer.read(paramDataInput)
                        val `val` = valSer.read(paramDataInput)
                        map.put(key, `val`)
                    }
                    return map
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                }
                return map
            }
        })
    }

    private fun registerCustomClass() {
        AbstractPluginData.setSerializer(PlayerInfo::class.java, object : TypeSerializer<PlayerInfo> {
            @Throws(IOException::class)
            override fun write(paramDataOutput: GameOutputStream, objectParam: PlayerInfo) {
                AbstractPluginData.getSerializer(String::class.java)!!.write(paramDataOutput, objectParam.uuid)
                paramDataOutput.writeLong(objectParam.timesKicked)
                paramDataOutput.writeLong(objectParam.timesJoined)
                paramDataOutput.writeLong(objectParam.timeMute)
                paramDataOutput.writeBoolean(objectParam.admin)
            }

            @Throws(IOException::class)
            override fun read(paramDataInput: GameInputStream): PlayerInfo {
                val objectParam = PlayerInfo(AbstractPluginData.getSerializer(String::class.java)!!.read(paramDataInput) as String)
                objectParam.timesKicked = paramDataInput.readLong()
                objectParam.timesJoined = paramDataInput.readLong()
                objectParam.timeMute = paramDataInput.readLong()
                objectParam.admin = paramDataInput.readBoolean()
                return objectParam
            }
        })

        AbstractPluginData.setSerializer(PlayerAdminInfo::class.java, object : TypeSerializer<PlayerAdminInfo> {
            @Throws(IOException::class)
            override fun write(paramDataOutput: GameOutputStream, objectParam: PlayerAdminInfo) {
                paramDataOutput.writeString(objectParam.uuid)
                paramDataOutput.writeBoolean(objectParam.admin)
                paramDataOutput.writeBoolean(objectParam.superAdmin)
            }

            @Throws(IOException::class)
            override fun read(paramDataInput: GameInputStream): PlayerAdminInfo {
                return PlayerAdminInfo(paramDataInput.readString(), paramDataInput.readBoolean(), paramDataInput.readBoolean())
            }
        })

        AbstractPluginData.setSerializer(Packet::class.java, Packet.serializer)
        AbstractPluginData.setSerializer(NetConnectProofOfWork::class.java, NetConnectProofOfWork.serializer)
    }

    @Throws(ClassNotFoundException::class)
    fun lookup(name: String): Class<*> {
        return Class.forName(name)
    }

    fun getError(className: String): IllegalArgumentException {
        return IllegalArgumentException("$className does not have a serializer registered!")
    }
}