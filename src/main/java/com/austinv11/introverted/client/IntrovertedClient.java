package com.austinv11.introverted.client;

import com.austinv11.introverted.networking.Packet;
import com.austinv11.introverted.networking.PacketHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class IntrovertedClient implements PacketHandler {

    private final int serverPort, clientPort;
    private final List<Consumer<Packet>> consumers = new CopyOnWriteArrayList<>();

    public IntrovertedClient(int serverPort, int clientPort) {
        this.serverPort = serverPort;
        this.clientPort = clientPort;
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getClientPort() {
        return clientPort;
    }

    @Override
    public void send(Packet packet) {

    }

    @Override
    public void handle(Consumer<Packet> packetConsumer) {
        consumers.add(packetConsumer);
    }
}
