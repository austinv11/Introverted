package com.austinv11.introverted.networking.packets;

import com.austinv11.introverted.common.Introverted;
import com.austinv11.introverted.mapping.Serialized;
import com.austinv11.introverted.networking.Packet;
import com.austinv11.introverted.networking.PacketType;
import com.austinv11.introverted.networking.TraceablePacket;

/**
 * This is sent by the server to the client in order to refuse a connection attempt by a client.
 */
public class HandshakeRefusePacket extends TraceablePacket {

    @Serialized(0)
    private final String reason;

    HandshakeRefusePacket() {
        super();
        reason = null;
    }

    public HandshakeRefusePacket(String reason, long id) {
        super(Introverted.VERSION, PacketType.HANDSHAKE_REFUSE, id);
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
