package com.austinv11.introverted.networking;

import com.austinv11.introverted.mapping.Reflector;
import com.austinv11.introverted.mapping.Serialized;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

public class PacketInputStream implements Closeable {

    private static final int BUFFER_SIZE = 512;

    private final InputStream backing;

    public PacketInputStream(InputStream backing) {
        this.backing = backing;
    }

    public Packet read() throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int len = backing.read(buffer, 0, 6);

        if (len != 5)
            throw new IOException("Unable to decode packet!");

        int size = 0;
        size |= (buffer[2] & 0xff);
        size <<= 8;
        size |= (buffer[3] & 0xff);
        size <<= 8;
        size |= (buffer[4] & 0xff);
        size <<= 8;
        size |= (buffer[5] & 0xff);

        byte[] total = new byte[size+6];

        for (int i = 0; i < 6; i++) //Copy metadata
            total[i] = buffer[i];

        int read = 0;
        while (read < size) {
            int count = backing.read(buffer, 0, Math.min(BUFFER_SIZE, size - read));
            for (int i = 0; i < count; i++)
                total[read + i] = buffer[i];
            read += count;
        }

        PacketBuffer buf = new PacketBuffer(total);
        byte ver = buf.getVersion();
        PacketType type = buf.getOp();
        Reflector reflector = Reflector.instance();
        Packet packet = reflector.instantiate(type.getType());
        reflector.put(type.getType(), packet, "version", ver);
        reflector.put(type.getType(), packet, "type", type);
        for (Field field : reflector.getFields(type.getType(), Serialized.class))
            reflector.put(type.getType(), packet, field.getName(), buf.getNext());

        return packet;
    }

    @Override
    public void close() throws IOException {
        backing.close();
    }
}
