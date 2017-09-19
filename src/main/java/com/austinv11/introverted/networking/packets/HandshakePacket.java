package com.austinv11.introverted.networking.packets;

import com.austinv11.introverted.common.Introverted;
import com.austinv11.introverted.networking.Packet;
import com.austinv11.introverted.networking.PacketType;

public class HandshakePacket extends Packet {

    public HandshakePacket() {
        super(Introverted.VERSION, PacketType.HANDSHAKE);
    }
}
