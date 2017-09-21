package com.austinv11.introverted.client;

import com.austinv11.introverted.networking.*;
import com.austinv11.introverted.networking.packets.DiscoveryConfirmPacket;
import com.austinv11.introverted.networking.packets.DiscoveryPacket;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class IntrovertedClient implements PacketHandler {

    private static final int DISCOVERY_TIMEOUT_MS = 5; //Local servers should face no latency, so we are technically being generous

    private final List<Consumer<Packet>> consumers = new CopyOnWriteArrayList<>();
    private final PacketSocket socket;
    private final ExecutorService readService = Executors.newSingleThreadExecutor();
    private volatile boolean isClosed = false;

    public static List<Pair<String, Integer>> findTCPServers(int lowerBound, int upperBound) {
        List<Pair<String, Integer>> foundServers = new ArrayList<>();
        for (int i = lowerBound; i <= upperBound; i++) {
            try (IntrovertedClient tempClient = new IntrovertedClient(i)) {
                tempClient.send(new DiscoveryPacket());
                DiscoveryConfirmPacket confirmation
                        = (DiscoveryConfirmPacket) tempClient.waitFor(packet -> packet.getType() == PacketType.DISCOVERY_CONFIRM, DISCOVERY_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                if (confirmation != null)
                    foundServers.add(Pair.of(confirmation.getPlatformIdentifier(), i));
            } catch (Throwable t) {}
        }
        return foundServers;
    }

    public static List<Pair<String, Integer>> findTCPServers() {
        return findTCPServers(1, 9999);
    }

    public static List<Pair<String, String>> findUnixServers() {
        File tmpDir = new File("/tmp/");
        String[] sockets = tmpDir.list((dir, name) -> dir.getName().equals("/tmp/") && name.contains("introverted") && name.endsWith(".sock"));
        List<Pair<String, String>> foundServers = new ArrayList<>();
        if (sockets != null) {
            for (String socket : sockets) {
                try (IntrovertedClient tempClient = new IntrovertedClient(socket)) {
                    tempClient.send(new DiscoveryPacket());
                    DiscoveryConfirmPacket confirmation
                            = (DiscoveryConfirmPacket) tempClient.waitFor(packet -> packet.getType() == PacketType.DISCOVERY_CONFIRM, DISCOVERY_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                    if (confirmation != null)
                        foundServers.add(Pair.of(confirmation.getPlatformIdentifier(), socket));
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        return foundServers;
    }

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
                    if (packet != null) //Ignore null packets as they are likely due to the stream being terminated
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
        try {
            socket.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
