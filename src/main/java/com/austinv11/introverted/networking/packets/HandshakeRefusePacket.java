package com.austinv11.introverted.networking.packets;

import com.austinv11.introverted.common.Introverted;
import com.austinv11.introverted.mapping.Serialized;
import com.austinv11.introverted.networking.Packet;
import com.austinv11.introverted.networking.PacketType;

public class HandshakeRefusePacket extends Packet {

    @Serialized
    private final String reason;

    HandshakeRefusePacket() {
        super();
        reason = null;
    }

    public HandshakeRefusePacket(String reason) {
        super(Introverted.VERSION, PacketType.HANDSHAKE_REFUSE);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
