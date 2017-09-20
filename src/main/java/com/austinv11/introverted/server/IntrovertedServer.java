package com.austinv11.introverted.server;

import com.austinv11.introverted.networking.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class IntrovertedServer implements PacketHandler {

    public static final String JVM_LIGHT_PLATFORM = "JVM-light";

    private final PacketServerSocket serverSocket;
    private final List<Consumer<Packet>> consumers = new CopyOnWriteArrayList<>();
    private final ExecutorService connectionService = Executors.newSingleThreadExecutor();
    private final Map<PacketSocket, ExecutorService> connections = Collections.synchronizedMap(new HashMap<>());
    private volatile boolean isClosed = false;

    public IntrovertedServer(int port) {
        serverSocket = PacketServerSocket.wrap(SocketFactory.newTCPServerSocket(port));
        _completeInit();
    }

    public IntrovertedServer(String unixSocketAddress) {
        serverSocket = PacketServerSocket.wrap(SocketFactory.newUnixServerSocket(unixSocketAddress));
        _completeInit();
    }

    private void _completeInit() {
        connectionService.execute(() -> {
            while (!isClosed()) {
                PacketSocket socket = serverSocket.accept();
                if (socket != null) { //Ignore null sockets as they are likely due to socket closures
                    ExecutorService readService = Executors.newSingleThreadExecutor();
                    connections.put(socket, readService);
                    readService.execute(() -> {
                            while (!isClosed()) {
                                Packet packet = null;
                                try {
                                    packet = socket.getInputStream().read();
                                    if (packet != null) { //Ignore null packets as they are likely due to the stream being terminated
                                        Packet finalPacket = packet;
                                        consumers.forEach(consumer -> consumer.accept(finalPacket));
                                    }
                                } catch (IOException e) {
                                    if (!isClosed())
                                        e.printStackTrace();
                                } finally {
                                    if (packet == null) {
                                        try {
                                            socket.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        connections.remove(socket);
                                        readService.shutdownNow();
                                        break;
                                    }
                                }
            //                    } finally { TODO re-implement
            //                        if (!isClosed()) {
            //                            try {
            //                                socket.close();
            //                            } catch (IOException e) {
            //                                e.printStackTrace();
            //                            }
            //                            connections.remove(socket);
            //                        }
                            }
                    });
                }
            }
        });
        handle(new ServerBasePacketConsumer(this));
    }

    @Override
    public synchronized void send(Packet packet) {
        connections.keySet().forEach(socket -> {
            socket.getOutputStream().write(packet);
            try {
                socket.getOutputStream().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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

        for (Map.Entry<PacketSocket, ExecutorService> connection : connections.entrySet()) {
            connection.getValue().shutdownNow();
            connection.getKey().close();
        }

        connectionService.shutdownNow();
        serverSocket.close();
    }
}
