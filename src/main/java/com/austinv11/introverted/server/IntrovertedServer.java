package com.austinv11.introverted.server;

import com.austinv11.introverted.networking.Packet;
import com.austinv11.introverted.networking.PacketHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class IntrovertedServer implements PacketHandler {

    private final int port;
    private final List<Consumer<Packet>> consumers = new CopyOnWriteArrayList<>();

    public IntrovertedServer(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    @Override
    public void send(Packet packet) {

    }

    @Override
    public void handle(Consumer<Packet> packetConsumer) {
        consumers.add(packetConsumer);
    }
}
