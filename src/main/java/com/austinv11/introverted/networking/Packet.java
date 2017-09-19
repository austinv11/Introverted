package com.austinv11.introverted.networking;

public abstract class Packet {

    private final int version;
    private final PacketType type;

    public Packet() {
        this(-1, null);
    }

    public Packet(int version, PacketType type) {
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
