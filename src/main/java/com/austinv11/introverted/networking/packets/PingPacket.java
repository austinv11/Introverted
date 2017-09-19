package com.austinv11.introverted.networking.packets;

import com.austinv11.introverted.common.Introverted;
import com.austinv11.introverted.mapping.Serialized;
import com.austinv11.introverted.networking.Packet;
import com.austinv11.introverted.networking.PacketType;

public class PingPacket extends Packet {

    @Serialized(unsigned = true)
    private final long identifier;

    public PingPacket() {
        super(Introverted.VERSION, PacketType.PING);
        identifier = System.currentTimeMillis();
    }

    public long getIdentifier() {
        return identifier;
    }

    public long getSendTime() {
        return identifier;
    }
}
