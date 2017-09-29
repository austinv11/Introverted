package com.austinv11.introverted.networking.packets;

import com.austinv11.introverted.common.Introverted;
import com.austinv11.introverted.mapping.Serialized;
import com.austinv11.introverted.networking.Packet;
import com.austinv11.introverted.networking.PacketType;

/**
 * This is sent by the server to the client in order to refuse a connection attempt by a client.
 */
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

    /**
     * The human-readable reason for the rejection.
     *
     * @return The reason string.
     */
    public String getReason() {
        return reason;
    }
}
