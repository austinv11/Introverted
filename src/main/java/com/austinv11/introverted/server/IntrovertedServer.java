package com.austinv11.introverted.server;

import com.austinv11.introverted.networking.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class IntrovertedServer implements PacketHandler {

    private final PacketServerSocket serverSocket;
    private final List<PacketSocket> connections = new CopyOnWriteArrayList<>();
    private final List<Consumer<Packet>> consumers = new CopyOnWriteArrayList<>();

    public IntrovertedServer(int port) {
        serverSocket = PacketServerSocket.wrap(SocketFactory.newTCPServerSocket(port));
        _completeInit();
    }

    public IntrovertedServer(String unixSocketAddress) {
        serverSocket = PacketServerSocket.wrap(SocketFactory.newUnixServerSocket(unixSocketAddress));
        _completeInit();
    }

    private void _completeInit() {
        Executors.newSingleThreadExecutor().execute(() -> {
            while (true) {
                PacketSocket socket = serverSocket.accept();
                connections.add(socket);
                Executors.newSingleThreadExecutor().execute(() -> {
                    try {
                        Packet packet = socket.getInputStream().read();
                        consumers.forEach(consumer -> consumer.accept(packet));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        connections.remove(socket);
                    }
                });
            }
        });
    }

    @Override
    public void send(Packet packet) {
        connections.forEach(socket -> socket.getOutputStream().write(packet));
    }

    @Override
    public void handle(Consumer<Packet> packetConsumer) {
        consumers.add(packetConsumer);
    }
}
