package com.austinv11.introverted.networking.packets;

import com.austinv11.introverted.common.Introverted;
import com.austinv11.introverted.mapping.Serialized;
import com.austinv11.introverted.networking.Packet;
import com.austinv11.introverted.networking.PacketType;

public class ConnectionKilledPacket extends Packet {

    @Serialized
    private final int exitCode;

    ConnectionKilledPacket() {
        super();
        exitCode = 0;
    }

    public ConnectionKilledPacket(int exitCode) {
        super(Introverted.VERSION, PacketType.CONNECTION_KILLED);
        this.exitCode = exitCode;
    }
}
