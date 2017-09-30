package com.austinv11.introverted.networking.packets;

import com.austinv11.introverted.common.Introverted;
import com.austinv11.introverted.networking.Packet;
import com.austinv11.introverted.networking.PacketType;
import com.austinv11.introverted.networking.TraceablePacket;

/**
 * This is sent from the client to a server in order to detect running servers.
 */
public class DiscoveryPacket extends TraceablePacket {

    public DiscoveryPacket() {
        super(Introverted.VERSION, PacketType.DISCOVERY);
    }
}
