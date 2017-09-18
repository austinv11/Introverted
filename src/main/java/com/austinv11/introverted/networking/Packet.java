package com.austinv11.introverted.networking;

public abstract class Packet {

    private final PacketType type;

    protected Packet(PacketType type) {
        this.type = type;
    }

    public PacketType getType() {
        return type;
    }
}
