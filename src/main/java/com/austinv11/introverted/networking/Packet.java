package com.austinv11.introverted.networking;

import com.austinv11.introverted.mapping.Serialized;

/**
 * This is the base class for all packets sent over Introverted sockets.
 */
public abstract class Packet {

    //Special cases exist for these two fields but I am marking them for consistency sake
    @Serialized(0)
    private final byte version;
    @Serialized(1)
    private final PacketType type;

    public Packet() {
        this((byte) 0, null);
    }

    public Packet(byte version, PacketType type) {
        this.version = version;
        this.type = type;
    }

    /**
     * Gets the type of packet this represents.
     *
     * @return The packet type.
     */
    public PacketType getType() {
        return type;
    }

    /**
     * Gets the version of the Introverted protocol from the side this was sent from.
     *
     * @return The version. This should probably match {@link com.austinv11.introverted.common.Introverted#VERSION}.
     */
    public int getProtocolVersion() {
        return version;
    }
}
