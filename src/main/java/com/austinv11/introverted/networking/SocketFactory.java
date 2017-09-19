package com.austinv11.introverted.networking;

import jnr.ffi.Platform;
import jnr.unixsocket.*;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketFactory {

    public static Socket newTCPSocket(int port) {
        try {
            return new Socket("localhost", port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ServerSocket newTCPServerSocket() {
        return newTCPServerSocket(discoverOpenPort());
    }

    public static ServerSocket newTCPServerSocket(int port) {
        try {
            return new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static UnixSocket newUnixSocket() {
        return newUnixSocket(generateUnixSocketAddress());
    }

    public static UnixSocket newUnixSocket(String address) {
        try {
            File file = new File(address);
            file.deleteOnExit();
            return UnixSocketChannel.open(new UnixSocketAddress(file)).socket();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static UnixServerSocket newUnixServerSocket() {
        return newUnixServerSocket(generateUnixSocketAddress());
    }

    public static UnixServerSocket newUnixServerSocket(String address) {
        try {
            File file = new File(address);
            file.deleteOnExit();
            UnixServerSocket socket = UnixServerSocketChannel.open().socket();
            socket.bind(new UnixSocketAddress(file));
            return socket;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int discoverOpenPort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getJvmPid() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        if (name.contains("@"))
            name = name.split("@")[0];
        return name;
    }

    public static String generateUnixSocketAddress() {
        return String.format("/tmp/introverted@%s.sock", getJvmPid());
    }

    public static boolean supportsUnixSockets() {
        return Platform.getNativePlatform().isUnix();
    }
}
