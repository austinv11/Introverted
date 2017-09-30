package com.austinv11.introverted.networking.packets;

import com.austinv11.introverted.common.Introverted;
import com.austinv11.introverted.networking.Packet;
import com.austinv11.introverted.networking.PacketType;
import com.austinv11.introverted.networking.TraceablePacket;

/**
 * This is sent from a server to a client in order to confirm a connection attempt.
 */
public class HandshakeConfirmPacket extends TraceablePacket {

    public HandshakeConfirmPacket(long id) {
        super(Introverted.VERSION, PacketType.HANDSHAKE_CONFIRM, id);
    }
}
