package com.austinv11.introverted.networking;

import jnr.unixsocket.UnixSocket;
import jnr.unixsocket.UnixSocketAddress;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

/**
 * Represents a common interface for any backing communication method.
 */
public interface PacketSocket extends Closeable {

    /**
     * Creates a packet socket which transports via unix sockets.
     *
     * @param socket The unix socket to use.
     * @return The new, wrapped implementation of the socket.
     */
    static PacketSocket wrap(UnixSocket socket) {
        try {
            return new UnixPacketSocket(socket);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a packet socket which transports via TCP sockets.
     *
     * @param socket The unix socket to use.
     * @return The new, wrapped implementation of the socket.
     */
    static PacketSocket wrap(Socket socket) {
        try {
            return new TCPPacketSocket(socket);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the address of the socket. Either "localhost:$port" for tcp or the path to the unix socket.
     *
     * @return The address.
     */
    String getAddress();

    /**
     * Gets the socket's input stream.
     *
     * @return The packet input stream.
     */
    PacketInputStream getInputStream();

    /**
     * Gets the socket's output stream.
     *
     * @return The packet output stream.
     */
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