package com.austinv11.introverted.networking;

import jnr.ffi.Platform;
import jnr.unixsocket.*;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This is a factory class to simplify socket construction.
 */
public class SocketFactory {

    /**
     * Creates a new tcp socket pointing towards localhost with the provided port.
     *
     * @param port The port to use.
     * @return The new socket.
     */
    public static Socket newTCPSocket(int port) {
        try {
            return new Socket("localhost", port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new tcp server socket pointing towards localhost and an automatically found port.
     *
     * @return The new socket.
     */
    public static ServerSocket newTCPServerSocket() {
        return newTCPServerSocket(discoverOpenPort());
    }

    /**
     * Creates a new tcp server socket pointing towards localhost and a provided port.
     *
     * @param port The port to bind the server socket to.
     * @return The new socket.
     */
    public static ServerSocket newTCPServerSocket(int port) {
        try {
            return new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new unix socket pointing towards an address representing the current process.
     *
     * @return The new socket.
     */
    public static UnixSocket newUnixSocket() {
        return newUnixSocket(generateUnixSocketAddress());
    }

    /**
     * Creates a new unix socket pointing towards the passed address.
     *
     * @param address The address to use.
     * @return The new socket.
     */
    public static UnixSocket newUnixSocket(String address) {
        try {
            File file = new File(address);
            file.deleteOnExit();
            return UnixSocketChannel.open(new UnixSocketAddress(file)).socket();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new unix server socket pointing towards an address representing the current process.
     *
     * @return The new server socket.
     */
    public static UnixServerSocket newUnixServerSocket() {
        return newUnixServerSocket(generateUnixSocketAddress());
    }

    /**
     * Creates a new unix server socket pointing towards the passed address.
     *
     * @param address The address to use.
     * @return The new server socket.
     */
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

    /**
     * Attempts to discover an open port.
     *
     * @return The open port.
     */
    public static int discoverOpenPort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the current JVM process' PID.
     *
     * @return The PID.
     */
    public static String getJvmPid() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        if (name.contains("@"))
            name = name.split("@")[0];
        return name;
    }

    /**
     * Generates a unix socket address based on the current JVM process.
     *
     * @return The socket address.
     */
    public static String generateUnixSocketAddress() {
        return String.format("/tmp/introverted@%s.sock", getJvmPid());
    }

    /**
     * Checks if the current platform supports unix sockets.
     *
     * @return True if unix sockets are supported, false if otherwise.
     */
    public static boolean supportsUnixSockets() {
        return Platform.getNativePlatform().isUnix();
    }
}
