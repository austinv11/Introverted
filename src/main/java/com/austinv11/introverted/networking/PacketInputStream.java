package com.austinv11.introverted.networking;

import com.austinv11.introverted.mapping.Reflector;
import com.austinv11.introverted.mapping.Serialized;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

/**
 * This represents a psuedo-InputStream which can be used to read incoming packets from.
 */
public class PacketInputStream implements Closeable {

    private static final int BUFFER_SIZE = 512;

    private final InputStream backing;

    /**
     * Wraps the true input stream to read through.
     *
     * @param backing The backing input stream.
     */
    public PacketInputStream(InputStream backing) {
        this.backing = backing;
    }

    /**
     * Blocks until the next packet is received, at which point the packet is decoded.
     *
     * @return The packet read or null if the stream was terminated.
     *
     * @throws IOException
     */
    public Packet read() throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int len = backing.read(buffer, 0, 6);

        if (len == -1) //Stream terminated
            return null;

        if (len != 6 && len != 2) //len is 2 when there is no content to the packet (ex DISCOVERY packets)
            throw new IOException("Unable to decode packet!");

        int size = 0;
        if (len != 2) { //Only need to update size to be greater than zero if we actually have a size greater than 0
            size |= (buffer[2] & 0xff);
            size <<= 8;
            size |= (buffer[3] & 0xff);
            size <<= 8;
            size |= (buffer[4] & 0xff);
            size <<= 8;
            size |= (buffer[5] & 0xff);
        }

        size += 6; //Don't forget metadata

        byte[] total = new byte[size];

        for (int i = 0; i < 6; i++) //Copy metadata
            total[i] = buffer[i];

        int read = 6;
        while (read < size) {
            int count = backing.read(buffer, 0, Math.min(BUFFER_SIZE, size - read));
            for (int i = 0; i < count; i++) {
                total[read + i] = buffer[i];
            }
            read += count;
        }

        PacketBuffer buf = new PacketBuffer(total);
        byte ver = buf.getVersion();
        PacketType type = buf.getOp();
        Reflector reflector = Reflector.instance();
        Packet packet = reflector.instantiate(type.getType());
        try {
            reflector.put(type.getType(), packet, "version", ver);
            reflector.put(type.getType(), packet, "type", type);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        for (Field field : reflector.getSerializedFields(type.getType()))
            reflector.put(type.getType(), packet, field.getName(), buf.getNext());

        return packet;
    }

    @Override
    public void close() throws IOException {
        backing.close();
    }
}
