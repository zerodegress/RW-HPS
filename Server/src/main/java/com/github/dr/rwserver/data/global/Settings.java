package com.github.dr.rwserver.data.global;

import com.github.dr.rwserver.io.ReusableByteInStream;
import com.github.dr.rwserver.struct.ObjectMap;
import com.github.dr.rwserver.util.file.FileUtil;
import com.github.dr.rwserver.util.io.DefaultSerializers;
import com.github.dr.rwserver.util.log.Log;
import com.github.dr.rwserver.util.zip.gzip.GzipDecoder;
import com.github.dr.rwserver.util.zip.gzip.GzipEncoder;

import java.io.*;

/**
 * @author Dr
 */
@SuppressWarnings("unchecked")
public class Settings {
    private ObjectMap<String, Object> values = new ObjectMap();
    private ObjectMap<Class<?>, TypeSerializer<?>> serializers = new ObjectMap();
    private ReusableByteInStream byteInputStream = new ReusableByteInStream();
    private ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    private DataOutputStream dataOutput = new DataOutputStream(byteStream);

    public Settings() {
        DefaultSerializers.register(this);
    }

    public void load() {
        loadData();
    }

    public void saveData() {
        saveValues(FileUtil.File(Data.Plugin_Data_Path).toPath("Setting.bin"));
    }

    public void loadData() {
        loadValues(FileUtil.File(Data.Plugin_Data_Path).toPath("Setting.bin"));
    }

    public float getFloat(String name, float def) {
        return (Float) this.values.get(name, def);
    }
    
    public long getLong(String name, long def) {
        return (Long) this.values.get(name, def);
    }
    
    public Long getLong(String name) {
        return getLong(name, 0L);
    }
    
    public int getInt(String name, int def) {
        return (Integer) this.values.get(name, def);
    }
    
    public boolean getBool(String name, boolean def) {
        return (Boolean) this.values.get(name, def);
    }

    public byte[] getBytes(String name) {
        return (byte[])this.values.get(name, null);
    }
    
    public byte[] getBytes(String name, byte[] def) {
        return (byte[])this.values.get(name, def);
    }
    
    public String getString(String name, String def) {
        return String.valueOf(this.values.get(name, def));
    }

    public TypeSerializer getSerializer(Class type) {
        return (TypeSerializer)this.serializers.get(type);
    }

    public <T> void setSerializer(Class<T> type, final TypeWriter<T> writer, final TypeReader<T> reader) {
        this.serializers.put(type, new TypeSerializer<T>() {
            @Override
            public void write(DataOutput stream, T object) throws IOException {
                writer.write(stream, object);
            }
            @Override
            public T read(DataInput stream) throws IOException {
                return reader.read(stream);
            }
        });
    }

    public <T> void setSerializer(Class<?> type, TypeSerializer<T> ser) {
        this.serializers.put(type, ser);
    }

    public <T> T getObject(String name, Class<T> type, T def) {
        getSerializer(type);
        if (!this.serializers.containsKey(type)) {
            throw new IllegalArgumentException("Type " + type + " does not have a serializer registered!");
        }
        TypeSerializer serializer = (TypeSerializer)this.serializers.get(type);
        try {
            this.byteInputStream.setBytes(getBytes(name));
            Object obj = serializer.read(new DataInputStream(byteInputStream));
            if (obj == null) {
                return (T)def;
            }
            return (T)obj;
        } catch (Exception e) {
            return (T)def;
        }
    }

    public void putObject(String name, Object value) {
        putObject(name, value, value.getClass());
    }
    
    public void putObject(String name, Object value, Class<?> type) {
         getSerializer(type);
         if (!this.serializers.containsKey(type)) {
             throw new IllegalArgumentException(type + " does not have a serializer registered!");
         }
         this.byteStream.reset();
         TypeSerializer<Object> serializer = (TypeSerializer)this.serializers.get(type);
         try {
             serializer.write(this.dataOutput, value);
             put(name, this.byteStream.toByteArray());
         } catch (Exception e) {
             Log.error("Put Data",e);
         }
    }

    public void put(String name, Object object) {
        if (object instanceof Float || object instanceof Integer || object instanceof Boolean || object instanceof Long || object instanceof String || object instanceof byte[]) {
            this.values.put(name, object);
        } else {
            throw new IllegalArgumentException("Invalid object stored: " + ((object == null) ? null : object.getClass()) + ". Use putObject() for serialization.");
        }
    }

    private void saveValues(FileUtil fileUtil) {
        try(DataOutputStream stream = new DataOutputStream(GzipEncoder.getGzipOutputStream(fileUtil.writeByteOutputStream(false)))){
            stream.writeInt(values.size);

            for(ObjectMap.Entry<String, Object> entry : values.entries()){
                stream.writeUTF(entry.key);
                Object value = entry.value;
                if(value instanceof Boolean){
                    stream.writeByte(0);
                    stream.writeBoolean((Boolean)value);
                }else if(value instanceof Integer){
                    stream.writeByte(1);
                    stream.writeInt((Integer)value);
                }else if(value instanceof Long){
                    stream.writeByte(2);
                    stream.writeLong((Long)value);
                }else if(value instanceof Float){
                    stream.writeByte(3);
                    stream.writeFloat((Float)value);
                }else if(value instanceof String){
                    stream.writeByte(4);
                    stream.writeUTF((String)value);
                }else if(value instanceof byte[]){
                    stream.writeByte(5);
                    stream.writeInt(((byte[])value).length);
                    stream.write((byte[])value);
                }
            }
            stream.flush();
        }catch(Exception e){
            fileUtil.getFile().delete();
            Log.error("Write Data",e);
            throw new RuntimeException();
        }
    }

    public void loadValues(FileUtil fileUtil) {
        try (DataInputStream stream = new DataInputStream(GzipDecoder.getGzipInputStream(fileUtil.getInputsStream()))) {
            int amount = stream.readInt();

            for (int i = 0; i < amount; i++) {
                int length; byte[] bytes; String key = stream.readUTF();
                byte type = stream.readByte();
                switch (type) {
                    case 0:
                        this.values.put(key, stream.readBoolean());
                        break;
                    case 1:
                        this.values.put(key, stream.readInt());
                        break;
                    case 2:
                        this.values.put(key, stream.readLong());
                        break;
                    case 3:
                        this.values.put(key, stream.readFloat());
                        break;
                    case 4:
                        this.values.put(key, stream.readUTF());
                        break;
                    case 5:
                        length = stream.readInt();
                        bytes = new byte[length];
                        stream.read(bytes);
                        this.values.put(key, bytes);
                        break;
                    }
                }
        } catch (Exception e) {
            Log.error("Read Data",e);
        }
    }

    public static interface TypeSerializer<T> {
        void write(DataOutput param1DataOutput, T param1T) throws IOException;
        T read(DataInput param1DataInput) throws IOException;
    }

    public static interface TypeWriter<T> {
        void write(DataOutput param1DataOutput, T param1T) throws IOException;
    }
    public static interface TypeReader<T> {
        T read(DataInput param1DataInput) throws IOException;
    }
}
