package com.austinv11.introverted.networking;

import jnr.unixsocket.UnixServerSocket;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;

/**
 * Represents a common interface for any backing communication method.
 */
public interface PacketServerSocket extends Closeable {

    /**
     * Creates a packet server socket which transports via unix sockets.
     *
     * @param socket The unix socket to use.
     * @return The new, wrapped implementation of the server socket.
     */
    static PacketServerSocket wrap(UnixServerSocket socket) {
        return new UnixPacketServerSocket(socket);
    }

    /**
     * Creates a packet server socket which transports via tcp sockets.
     *
     * @param socket The tcp socket to use.
     * @return The new, wrapped implementation of the server socket.
     */
    static PacketServerSocket wrap(ServerSocket socket) {
        return new TCPPacketServerSocket(socket);
    }

    /**
     * This blocks until a connection is attempted to this server socket.
     *
     * @return The socket representing the connection accepted.
     */
    PacketSocket accept();
}

class TCPPacketServerSocket implements PacketServerSocket {

    private final ServerSocket tcp;

    TCPPacketServerSocket(ServerSocket tcp) {
        this.tcp = tcp;
    }

    @Override
    public PacketSocket accept() {
        try {
            if (tcp.isClosed())
                return null;

            return PacketSocket.wrap(tcp.accept());
        } catch (IOException e) {
            if (!tcp.isClosed())
                throw new RuntimeException(e);
            else
                return null;
        }
    }

    @Override
    public void close() throws IOException {
        tcp.close();
    }
}

class UnixPacketServerSocket implements PacketServerSocket {

    private final UnixServerSocket unix;

    UnixPacketServerSocket(UnixServerSocket unix) {
        this.unix = unix;
    }

    @Override
    public PacketSocket accept() {
        try {
            return PacketSocket.wrap(unix.accept());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        //NO-OP currently since UnixServerSocket does not implement Closeable
    }
}
