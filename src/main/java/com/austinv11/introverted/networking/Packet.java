package com.austinv11.introverted.networking;

public abstract class Packet {

    private final byte version;
    private final PacketType type;

    public Packet() {
        this((byte) 0, null);
    }

    public Packet(byte version, PacketType type) {
        this.version = version;
        this.type = type;
    }

    public PacketType getType() {
        return type;
    }

    public int getProtocolVersion() {
        return version;
    }
}
