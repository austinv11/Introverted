package com.austinv11.introverted.networking.packets;

import com.austinv11.introverted.common.Introverted;
import com.austinv11.introverted.mapping.Serialized;
import com.austinv11.introverted.networking.Packet;
import com.austinv11.introverted.networking.PacketType;

/**
 * This is sent by any side in order to ensure that a connection is still alive.
 */
public class PingPacket extends Packet {

    @Serialized(unsigned = true)
    private final long identifier;

    public PingPacket() {
        super(Introverted.VERSION, PacketType.PING);
        identifier = System.currentTimeMillis();
    }

    /**
     * A unique identifier to discriminate between multiple ping pongs.
     *
     * @return The unique identifier.
     */
    public long getIdentifier() {
        return identifier;
    }

    /**
     * This gets the time the ping was sent in epoch milliseconds.
     *
     * @return The send time.
     */
    public long getSendTime() {
        return identifier;
    }
}
