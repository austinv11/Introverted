package com.austinv11.introverted.networking.packets;

import com.austinv11.introverted.common.Introverted;
import com.austinv11.introverted.mapping.Serialized;
import com.austinv11.introverted.networking.Packet;
import com.austinv11.introverted.networking.PacketType;

public class PongPacket extends Packet {

    @Serialized(unsigned = true)
    private final long identifier;
    private final long sendTime;

    PongPacket() {
        super();
        this.identifier = -1;
        this.sendTime = 0;
    }

    public PongPacket(long identifer) {
        super(Introverted.VERSION, PacketType.PONG);
        this.identifier = identifer;
        this.sendTime = System.currentTimeMillis();
    }

    public long getIdentifier() {
        return identifier;
    }

    public long getSendTime() {
        return sendTime;
    }
}
