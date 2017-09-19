package com.austinv11.introverted.networking;

import jnr.unixsocket.UnixSocket;
import jnr.unixsocket.UnixSocketAddress;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

public interface PacketSocket extends Closeable {

    static PacketSocket wrap(UnixSocket socket) {
        try {
            return new UnixPacketSocket(socket);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static PacketSocket wrap(Socket socket) {
        try {
            return new TCPPacketSocket(socket);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    String getAddress();

    PacketInputStream getInputStream();

    PacketOutputStream getOutputStream();
}

class TCPPacketSocket implements PacketSocket {

    private final Socket tcp;
    private final PacketInputStream inputStream;
    private final PacketOutputStream outputStream;

    TCPPacketSocket(Socket tcp) throws IOException {
        this.tcp = tcp;
        inputStream = new PacketInputStream(tcp.getInputStream());
        outputStream = new PacketOutputStream(tcp.getOutputStream());
    }

    @Override
    public String getAddress() {
        return String.format("localhost:%s", tcp.getPort());
    }

    @Override
    public PacketInputStream getInputStream() {
        return inputStream;
    }

    @Override
    public PacketOutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
        outputStream.close();
        tcp.close();
    }
}

class UnixPacketSocket implements PacketSocket {

    private final UnixSocket unix;
    private final PacketInputStream inputStream;
    private final PacketOutputStream outputStream;

    UnixPacketSocket(UnixSocket unix) throws IOException {
        this.unix = unix;
        inputStream = new PacketInputStream(unix.getInputStream());
        outputStream = new PacketOutputStream(unix.getOutputStream());
    }

    @Override
    public String getAddress() {
        return ((UnixSocketAddress) unix.getRemoteSocketAddress()).path();
    }

    @Override
    public PacketInputStream getInputStream() {
        return inputStream;
    }

    @Override
    public PacketOutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
        outputStream.close();
        unix.close();
    }
}