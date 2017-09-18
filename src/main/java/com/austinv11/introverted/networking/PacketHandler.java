package com.austinv11.introverted.networking;

import java.util.function.Consumer;

public interface PacketHandler {

    void send(Packet packet);

    default <T extends Packet> void handle(Class<T> packetType, Consumer<T> packetHandler) {
        handle(p -> {
            if (p.getClass().equals(packetType))
                packetHandler.accept((T) p);
        });
    }

    void handle(Consumer<Packet> packetConsumer);
}
