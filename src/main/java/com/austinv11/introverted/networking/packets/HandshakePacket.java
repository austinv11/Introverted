package com.austinv11.introverted.networking.packets;

import com.austinv11.introverted.common.Introverted;
import com.austinv11.introverted.networking.Packet;
import com.austinv11.introverted.networking.PacketType;
import com.austinv11.introverted.networking.TraceablePacket;

/**
 * This is sent from the client to a server to attempt to start an active connection.
 */
public class HandshakePacket extends TraceablePacket {

    public HandshakePacket() {
        super(Introverted.VERSION, PacketType.HANDSHAKE);
    }
}
