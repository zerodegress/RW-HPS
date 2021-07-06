package com.github.dr.rwserver.data.plugin;

import com.github.dr.rwserver.net.Administration;
import com.github.dr.rwserver.struct.ObjectMap;
import com.github.dr.rwserver.struct.OrderedMap;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.struct.SerializerTypeAll;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author Dr
 */
@SuppressWarnings("unchecked")
class DefaultSerializers {
    public static void registrationBasis() {
        AbstractPluginData.setSerializer(String.class, new SerializerTypeAll.TypeSerializer<String>() {
            @Override
            public void write(DataOutput stream, String object) throws IOException {
                stream.writeUTF((object == null) ? "" : object);
            }
            @Override
            public String read(DataInput stream) throws IOException {
                return stream.readUTF();
            }
        });
    }

    public static void signUpForAdvanced() {
        AbstractPluginData.setSerializer(Seq.class, new SerializerTypeAll.TypeSerializer<Seq>() {
            @Override
            public void write(DataOutput stream, Seq object) throws IOException {
                stream.writeInt(object.size());
                if (object.size() != 0) {
                    SerializerTypeAll.TypeSerializer ser = AbstractPluginData.getSerializer(object.get(0).getClass());
                    if (ser == null) {
                        throw new IllegalArgumentException(object.get(0).getClass() + " does not have a serializer registered!");
                    }
                    stream.writeUTF(object.get(0).getClass().getName());
                    for (Object element : object) {
                        ser.write(stream, element);
                    }
                }
            }
            @Override
            public Seq read(DataInput stream) throws IOException {
                try {
                    int size = stream.readInt();
                    Seq arr = new Seq(size);
                    if (size == 0) {
                        return arr;
                    }
                    String type = stream.readUTF();
                    SerializerTypeAll.TypeSerializer ser = AbstractPluginData.getSerializer(DefaultSerializers.lookup(type));
                    if (ser == null) {
                        throw new IllegalArgumentException(type + " does not have a serializer registered!");
                    }
                    for (int i = 0; i < size; i++) {
                        arr.add(ser.read(stream));
                    }
                    return arr;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        });

        AbstractPluginData.setSerializer(ObjectMap.class, new SerializerTypeAll.TypeSerializer<ObjectMap>() {
            @Override
            public void write(DataOutput stream, ObjectMap map) throws IOException {
                stream.writeInt(map.size);
                if (map.size == 0) {
                    return;
                }
                ObjectMap.Entry entry = map.entries().next();
                SerializerTypeAll.TypeSerializer keySer = AbstractPluginData.getSerializer(entry.key.getClass());
                SerializerTypeAll.TypeSerializer valSer = AbstractPluginData.getSerializer(entry.value.getClass());
                if (keySer == null) {
                    throw new IllegalArgumentException(entry.key.getClass() + " does not have a serializer registered!");
                }
                if (valSer == null) {
                    throw new IllegalArgumentException(entry.value.getClass() + " does not have a serializer registered!");
                }
                stream.writeUTF(entry.key.getClass().getName());
                stream.writeUTF(entry.value.getClass().getName());
                for (Object e : map.entries()) {
                    ObjectMap.Entry en = (ObjectMap.Entry) e;
                    keySer.write(stream, en.key);
                    valSer.write(stream, en.value);
                }
            }
            @Override
            public ObjectMap read(DataInput stream) throws IOException {
                try {
                    int size = stream.readInt();
                    ObjectMap map = new ObjectMap();
                    if (size == 0) {
                        return map;
                    }
                    String keyt = stream.readUTF();
                    String valt = stream.readUTF();
                    SerializerTypeAll.TypeSerializer keySer = AbstractPluginData.getSerializer(DefaultSerializers.lookup(keyt));
                    SerializerTypeAll.TypeSerializer valSer = AbstractPluginData.getSerializer(DefaultSerializers.lookup(valt));
                    if (keySer == null) {
                        throw new IllegalArgumentException(keyt + " does not have a serializer registered!");
                    }
                    if (valSer == null) {
                        throw new IllegalArgumentException(valt + " does not have a serializer registered!");
                    }
                    for (int i = 0; i < size; i++) {
                        Object key = keySer.read(stream);
                        Object val = valSer.read(stream);
                        map.put(key, val);
                    }
                    return map;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        });

        AbstractPluginData.setSerializer(OrderedMap.class, new SerializerTypeAll.TypeSerializer<OrderedMap>() {
            @Override
            public void write(DataOutput stream, OrderedMap map) throws IOException {
                stream.writeInt(map.size);
                if (map.size == 0) {
                    return;
                }
                OrderedMap.Entry entry = map.entries().next();
                SerializerTypeAll.TypeSerializer keySer = AbstractPluginData.getSerializer(entry.key.getClass());
                SerializerTypeAll.TypeSerializer valSer = AbstractPluginData.getSerializer(entry.value.getClass());
                if (keySer == null) {
                    throw new IllegalArgumentException(entry.key.getClass() + " does not have a serializer registered!");
                }
                if (valSer == null) {
                    throw new IllegalArgumentException(entry.value.getClass() + " does not have a serializer registered!");
                }
                stream.writeUTF(entry.key.getClass().getName());
                stream.writeUTF(entry.value.getClass().getName());
                for (Object e : map.entries()) {
                    OrderedMap.Entry en = (OrderedMap.Entry) e;
                    keySer.write(stream, en.key);
                    valSer.write(stream, en.value);
                }
            }
            @Override
            public OrderedMap read(DataInput stream) throws IOException {
                try {
                    int size = stream.readInt();
                    OrderedMap map = new OrderedMap();
                    if (size == 0) {
                        return map;
                    }
                    String keyt = stream.readUTF();
                    String valt = stream.readUTF();
                    SerializerTypeAll.TypeSerializer keySer = com.github.dr.rwserver.data.plugin.AbstractPluginData.getSerializer(DefaultSerializers.lookup(keyt));
                    SerializerTypeAll.TypeSerializer valSer = AbstractPluginData.getSerializer(DefaultSerializers.lookup(valt));
                    if (keySer == null) {
                        throw new IllegalArgumentException(keyt + " does not have a serializer registered!");
                    }
                    if (valSer == null) {
                        throw new IllegalArgumentException(valt + " does not have a serializer registered!");
                    }
                    for (int i = 0; i < size; i++) {
                        Object key = keySer.read(stream);
                        Object val = valSer.read(stream);
                        map.put(key, val);
                    }
                    return map;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        });

        AbstractPluginData.setSerializer(Administration.PlayerInfo.class, new SerializerTypeAll.TypeSerializer<Administration.PlayerInfo>() {
            @Override
            public void write(DataOutput stream, Administration.PlayerInfo object) throws IOException {
                AbstractPluginData.getSerializer(String.class).write(stream, object.uuid);
                stream.writeLong(object.timesKicked);
                stream.writeLong(object.timesJoined);
                stream.writeLong(object.timeMute);
                stream.writeBoolean(object.admin);
            }

            @Override
            public Administration.PlayerInfo read(DataInput stream) throws IOException {
                Administration.PlayerInfo object = new Administration.PlayerInfo((String)AbstractPluginData.getSerializer(String.class).read(stream));
                object.timesKicked = stream.readLong();
                object.timesJoined = stream.readLong();
                object.timeMute = stream.readLong();
                object.admin = stream.readBoolean();
                return object;
            }
        });
    }

    private static Class<?> lookup(String name) throws ClassNotFoundException {
        return Class.forName(name);
   }
}
