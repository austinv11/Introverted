package com.austinv11.introverted.networking;

import com.austinv11.introverted.mapping.Reflector;
import com.austinv11.introverted.mapping.Serialized;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.List;

public class PacketOutputStream implements Closeable, Flushable {

    private final PacketBuffer buf;
    private final OutputStream backing;

    public PacketOutputStream(PacketBuffer buf, OutputStream backing) {
        this.buf = buf;
        this.backing = backing;
    }

    public PacketOutputStream(OutputStream backing) {
        this(new PacketBuffer(), backing);
    }

    @Override
    public void close() throws IOException {
        flush();
        backing.close();
    }

    @Override
    public void flush() throws IOException {
        backing.write(buf.flush());
        backing.flush();
    }

    public <T extends Packet> void write(T packet) {
        List<Field> fields = Reflector.instance().getFields(packet.getClass(), Serialized.class);

        if (buf.size() == 0)
            buf.putVersion();

        if (buf.size() == 1)
            buf.putOp(packet.getType());

        for (Field field : fields)
            buf.put(Reflector.instance().get(packet.getClass(), packet, field.getName()));
    }
}
