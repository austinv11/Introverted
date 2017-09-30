package com.austinv11.introverted.networking.packets;

import com.austinv11.introverted.common.Introverted;
import com.austinv11.introverted.mapping.Serialized;
import com.austinv11.introverted.networking.Packet;
import com.austinv11.introverted.networking.PacketType;
import com.austinv11.introverted.networking.TraceablePacket;

/**
 * This is sent in response to a {@link PingPacket}.
 */
public class PongPacket extends TraceablePacket {

    @Serialized(value = 0, unsigned = true)
    private final long sendTime;

    PongPacket() {
        super();
        sendTime = 0;
    }

    public PongPacket(long identifer) {
        super(Introverted.VERSION, PacketType.PONG, identifer);
        this.sendTime = System.currentTimeMillis();
    }

    /**
     * This gets the time the pong was sent in epoch milliseconds.
     *
     * @return The send time.
     */
    public long getSendTime() {
        return sendTime;
    }
}
