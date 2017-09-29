package com.austinv11.introverted.client;

import com.austinv11.introverted.networking.*;
import com.austinv11.introverted.networking.packets.DiscoveryConfirmPacket;
import com.austinv11.introverted.networking.packets.DiscoveryPacket;
import com.austinv11.introverted.networking.packets.HandshakePacket;
import com.austinv11.introverted.networking.packets.HandshakeRefusePacket;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * This handles client side communication to an Introverted server.
 *
 * Client side = observer/mutator of a running process (not necessarily the process itself).
 */
public class IntrovertedClient implements PacketHandler {

    private static final int DISCOVERY_TIMEOUT_MS = 5; //Local servers should face no latency, so we are technically being generous

    private final List<Consumer<Packet>> consumers = new CopyOnWriteArrayList<>();
    private final PacketSocket socket;
    private final ExecutorService readService = Executors.newSingleThreadExecutor();
    private volatile boolean isClosed = false;

    /**
     * This will attempt to search for local Introverted servers which are running through TCP.
     *
     * <b>This can take awhile when bounds have a high range!</b>
     *
     * @param lowerBound The lower port bound (this should be greater than zero!).
     * @param upperBound The higher port bound (this should be less than or equal to 9999!).
     * @return The list of found servers with description pairs (left = platform identifier, right = port).
     */
    public static List<Pair<String, Integer>> findTCPServers(int lowerBound, int upperBound) {
        List<Pair<String, Integer>> foundServers = new ArrayList<>();
        for (int i = lowerBound; i <= upperBound; i++) {
            try (IntrovertedClient tempClient = new IntrovertedClient(i)) {
                DiscoveryConfirmPacket confirmation = (DiscoveryConfirmPacket) tempClient.exchange(new DiscoveryPacket(),
                        packet -> packet.getType() == PacketType.DISCOVERY_CONFIRM, DISCOVERY_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                if (confirmation != null)
                    foundServers.add(Pair.of(confirmation.getPlatformIdentifier(), i));
            } catch (Throwable t) {}
        }
        return foundServers;
    }

    /**
     * Attempts to search for local Introverted servers running through TCP from the port 0001 to 9999.
     *
     * <b>This is likely to take awhile so it is not recommended! If you have to search, do some manual heuristics!</b>
     *
     * @return The list of found servers with description pairs (left = platform identifier, right = port).
     */
    public static List<Pair<String, Integer>> findTCPServers() {
        return findTCPServers(1, 9999);
    }

    /**
     * Attempts to search for local Introverted servers running through unix sockets (AF_UNIX).
     *
     * <b>This expects servers to have an address located in in the /tmp/ directory, contains the string 'introverted'
     * and ends with '.sock'</b>
     *
     * @return The list of found servers with description pairs (left = platform identifier, right = address).
     */
    public static List<Pair<String, String>> findUnixServers() {
        File tmpDir = new File("/tmp/");
        String[] sockets = tmpDir.list((dir, name) -> dir.getName().equals("/tmp/") && name.contains("introverted") && name.endsWith(".sock"));
        List<Pair<String, String>> foundServers = new ArrayList<>();
        if (sockets != null) {
            for (String socket : sockets) {
                try (IntrovertedClient tempClient = new IntrovertedClient(socket)) {
                    tempClient.send(new DiscoveryPacket());
                    DiscoveryConfirmPacket confirmation = (DiscoveryConfirmPacket) tempClient.exchange(new DiscoveryPacket(),
                            packet -> packet.getType() == PacketType.DISCOVERY_CONFIRM, DISCOVERY_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                    if (confirmation != null)
                        foundServers.add(Pair.of(confirmation.getPlatformIdentifier(), socket));
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        return foundServers;
    }

    /**
     * This creates a client which attempts to make a TCP connection to an Introverted server.
     *
     * @param serverPort The port of the server.
     */
    public IntrovertedClient(int serverPort) {
        socket = PacketSocket.wrap(SocketFactory.newTCPSocket(serverPort));
        _completeInit();
    }

    /**
     * This creates a client which attempts to make a unix socket (AF_UNIX) connection to an Introverted server.
     *
     * @param unixSocketAddress The socket address of the server.
     */
    public IntrovertedClient(String unixSocketAddress) {
        socket = PacketSocket.wrap(SocketFactory.newUnixSocket(unixSocketAddress));
        _completeInit();
    }

    private void _completeInit() { //Bootstrap socket listener and base consumer
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

    /**
     * This initiates the required handshake process for the client to complete its connection attempt to a server.
     *
     * @return An optional, the optional should be empty if the handshake was successful, otherwise it'll contain the
     * reason for it failing.
     *
     * @throws InterruptedException
     */
    public synchronized Optional<String> handshake() throws InterruptedException {
        Packet packet = exchange(new HandshakePacket(),
                p -> p.getType() == PacketType.HANDSHAKE_CONFIRM || p.getType() == PacketType.HANDSHAKE_REFUSE,
                DISCOVERY_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        if (packet.getType() == PacketType.HANDSHAKE_CONFIRM) {
            return Optional.empty();
        } else {
            return Optional.of(((HandshakeRefusePacket) packet).getReason());
        }
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
