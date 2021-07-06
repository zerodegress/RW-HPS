package com.github.dr.rwserver.struct;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class SerializerTypeAll {
    public interface TypeSerializer<T> {
        void write(DataOutput param1DataOutput, T param1T) throws IOException;
        T read(DataInput param1DataInput) throws IOException;
    }

    public interface TypeWriter<T> {
        void write(DataOutput param1DataOutput, T param1T) throws IOException;
    }
    public interface TypeReader<T> {
        T read(DataInput param1DataInput) throws IOException;
    }
}
