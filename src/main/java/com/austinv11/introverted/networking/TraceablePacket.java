package com.austinv11.introverted.networking;

import com.austinv11.introverted.mapping.Serialized;

import java.util.concurrent.ThreadLocalRandom;

/**
 * This represents a packet guaranteed to be trackable in {@link PacketHandler#exchange(TraceablePacket)} methods.
 */
public abstract class TraceablePacket extends Packet {

    @Serialized(value = -1, unsigned = true)
    private final long id;

    private static long generateId() {
        return ThreadLocalRandom.current().nextInt() & (System.nanoTime() << 16); //Make the last 4 bytes a random int, with the rest of the bytes seeded by the current nano time.
    }

    public TraceablePacket() {
        super();
        id = generateId();
    }

    public TraceablePacket(byte version, PacketType type) {
        this(version, type, generateId());
    }

    public TraceablePacket(byte version, PacketType type, long id) {
        super(version, type);
        this.id = id;
    }

    /**
     * Gets the id used as a unique identifier for the packet.
     *
     * @return The id (the sign is insignificant).
     */
    public long getId() {
        return id;
    }
}
