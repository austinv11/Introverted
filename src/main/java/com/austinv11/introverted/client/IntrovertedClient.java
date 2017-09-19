package com.austinv11.introverted.client;

import com.austinv11.introverted.networking.Packet;
import com.austinv11.introverted.networking.PacketHandler;
import com.austinv11.introverted.networking.PacketSocket;
import com.austinv11.introverted.networking.SocketFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class IntrovertedClient implements PacketHandler {

    private final List<Consumer<Packet>> consumers = new CopyOnWriteArrayList<>();
    private final PacketSocket socket;
    private final ExecutorService readService = Executors.newSingleThreadExecutor();
    private volatile boolean isClosed = false;

    public IntrovertedClient(int serverPort) {
        socket = PacketSocket.wrap(SocketFactory.newTCPSocket(serverPort));
        _completeInit();
    }

    public IntrovertedClient(String unixSocketAddress) {
        socket = PacketSocket.wrap(SocketFactory.newUnixSocket(unixSocketAddress));
        _completeInit();
    }

    private void _completeInit() {
        readService.execute(() -> {
            while (!isClosed()) {
                try {
                    Packet packet = socket.getInputStream().read();
                    consumers.forEach(consumer -> consumer.accept(packet));
                } catch (IOException e) {
                    if (!isClosed())
                        e.printStackTrace();
                }
            }
        });
        handle(new ClientBasePacketConsumer(this));
    }

    @Override
    public synchronized void send(Packet packet) {
        socket.getOutputStream().write(packet);
    }

    @Override
    public void handle(Consumer<Packet> packetConsumer) {
        consumers.add(packetConsumer);
    }

    @Override
    public void unregisterPacketConsumer(Consumer<Packet> packetConsumer) {
        consumers.remove(packetConsumer);
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public void close() throws IOException {
        isClosed = true;
        readService.shutdownNow();
        socket.close();
    }
}
