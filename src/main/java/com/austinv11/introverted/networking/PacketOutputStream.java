package com.austinv11.introverted.networking;

import com.austinv11.introverted.mapping.Reflector;
import com.austinv11.introverted.mapping.Serialized;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.List;

/**
 * This represents a psuedo-OutputStream which can be used to write packets to be sent.
 */
public class PacketOutputStream implements Closeable, Flushable {

    private final PacketBuffer buf;
    private final OutputStream backing;

    /**
     * Wraps an output stream and a packet buffer.
     *
     * @param buf The buffer to use.
     * @param backing The stream to write to.
     */
    public PacketOutputStream(PacketBuffer buf, OutputStream backing) {
        this.buf = buf;
        this.backing = backing;
    }

    /**
     * Wraps an output stream.
     *
     * @param backing The stream to write to.
     */
    public PacketOutputStream(OutputStream backing) {
        this(new PacketBuffer(), backing);
    }

    @Override
    public void close() throws IOException {
//        flush(); don't wanna flush us because incomplete packets may be send down the wire
        backing.close();
    }

    @Override
    public void flush() throws IOException {
        backing.write(buf.flush());
        backing.flush();
    }

    /**
     * Encodes and transmits a packet through to the other side.
     *
     * @param packet The packet to send.
     */
    public void write(Packet packet) {
        List<Field> fields = Reflector.instance().getSerializedFields(packet.getClass());

        if (buf.size() == 0)
            buf.putVersion();

        if (buf.size() == 1)
            buf.putOp(packet.getType());

        for (Field field : fields)
            buf.put(Reflector.instance().get(packet.getClass(), packet, field.getName()), field.getAnnotation(Serialized.class).unsigned());
    }
}
