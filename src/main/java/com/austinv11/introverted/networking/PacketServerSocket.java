package com.austinv11.introverted.networking;

import jnr.unixsocket.UnixServerSocket;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;

public interface PacketServerSocket extends Closeable {

    static PacketServerSocket wrap(UnixServerSocket socket) {
        return new UnixPacketServerSocket(socket);
    }

    static PacketServerSocket wrap(ServerSocket socket) {
        return new TCPPacketServerSocket(socket);
    }

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
            return PacketSocket.wrap(tcp.accept());
        } catch (IOException e) {
            throw new RuntimeException(e);
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
