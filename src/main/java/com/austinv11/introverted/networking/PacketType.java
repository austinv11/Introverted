package com.austinv11.introverted.networking;

public enum PacketType {
    //Reserved ops
    DISCOVERY(clazz), DISCOVERY_CONFIRM(clazz), HANDSHAKE(clazz), HANDSHAKE_CONFIRM(clazz), HANDSHAKE_REFUSE(clazz), PING(clazz), PONG(clazz), CONNECTION_KILLED(clazz);

    final Class<? extends Packet> clazz;

    PacketType(Class<? extends Packet> clazz) {
        this.clazz = clazz;
    }

    public Class<? extends Packet> getType() {
        return clazz;
    }
}
