package com.austinv11.introverted.networking.packets;

import com.austinv11.introverted.common.Introverted;
import com.austinv11.introverted.networking.Packet;
import com.austinv11.introverted.networking.PacketType;

public class HandshakeConfirmPacket extends Packet {

    public HandshakeConfirmPacket() {
        super(Introverted.VERSION, PacketType.HANDSHAKE_CONFIRM);
    }
}
