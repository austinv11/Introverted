package com.austinv11.introverted.networking;

import com.austinv11.introverted.networking.packets.*;

/**
 * An enumeration representing the possible packet types.
 *
 * @see <a href="https://github.com/austinv11/Introverted/blob/master/PROTOCOL.md">Introverted Protocol Specification</a>
 */
public enum PacketType {
    //Reserved ops
    DISCOVERY(DiscoveryPacket.class),
    DISCOVERY_CONFIRM(DiscoveryConfirmPacket.class),
    HANDSHAKE(HandshakePacket.class),
    HANDSHAKE_CONFIRM(HandshakeConfirmPacket.class),
    HANDSHAKE_REFUSE(HandshakeRefusePacket.class),
    PING(PingPacket.class),
    PONG(PongPacket.class),
    CONNECTION_KILLED(ConnectionKilledPacket.class);

    final Class<? extends Packet> clazz;

    PacketType(Class<? extends Packet> clazz) {
        this.clazz = clazz;
    }

    public Class<? extends Packet> getType() {
        return clazz;
    }
}
