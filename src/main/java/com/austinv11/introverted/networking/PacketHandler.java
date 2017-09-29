package com.austinv11.introverted.networking;

import com.austinv11.introverted.networking.packets.ConnectionKilledPacket;
import com.austinv11.introverted.networking.packets.PingPacket;
import com.austinv11.introverted.networking.packets.PongPacket;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * This represents a generic packet handler (either a client or server).
 */
public interface PacketHandler extends Closeable {

    /**
     * Sends a packet over the wire to a receiver.
     *
     * @param packet The packet to send.
     */
    void send(Packet packet);

    /**
     * This registers a packet listener.
     *
     * @param packetType The type of packets to listen to.
     * @param packetHandler The packet listener.
     */
    default <T extends Packet> void handle(PacketType packetType, Consumer<T> packetHandler) {
        handle(p -> {
            if (p.getType() == packetType)
                packetHandler.accept((T) p);
        });
    }

    /**
     * This registers a generic packet listener.
     *
     * @param packetConsumer The packet listener.
     */
    void handle(Consumer<Packet> packetConsumer);

    /**
     * Unregisters a specific instance of a packet listener.
     *
     * @param packetConsumer The packet listener.
     */
    void unregisterPacketConsumer(Consumer<Packet> packetConsumer);

    /**
     * Registers a handler which will automatically unregister via a the result of the listener.
     *
     * @param packetHandler The listener, returning true unregisters the listener.
     */
    default void temporarilyHandle(Predicate<Packet> packetHandler) {
        Consumer<Packet> handler = new Consumer<Packet>() {
            @Override
            public void accept(Packet packet) {
                if (packetHandler.test(packet))
                    unregisterPacketConsumer(this);
            }
        };

        handle(handler);
    }

    /**
     * This blocks the current thread until the passed predicate listening to packets returns true.
     *
     * @param packetPredicate The packet predicate.
     * @return The packet which caused the blocking to end.
     */
    default Packet waitFor(Predicate<Packet> packetPredicate) {
        try {
            return waitFor(packetPredicate, -1, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return null; //Should never happen
        }
    }


    /**
     * This blocks the current thread until the passed predicate listening to packets returns true.
     *
     * @param packetPredicate The packet predicate.
     * @param timeout The amount of time to wait before forcibly interrupting the blocking.
     * @param timeoutUnit The unit which the timeout represents.
     * @return The packet which caused the blocking to end.
     *
     * @throws InterruptedException
     */
    default Packet waitFor(Predicate<Packet> packetPredicate, long timeout, TimeUnit timeoutUnit) throws InterruptedException {
        return Support.waitFor(this, packetPredicate, timeout, timeoutUnit, Support.NO_OP_RUNNABLE);
    }

    /**
     * This blocks the current thread until the next packet of the given type is received.
     *
     * @param type The type of packet to wait for.
     * @param timeout The amount of time to wait before forcibly interrupting the blocking.
     * @param timeoutUnit The unit which the timeout represents.
     * @return The packet which caused the blocking to end.
     *
     * @throws InterruptedException
     */
    default <T extends Packet> T waitForNext(PacketType type, long timeout, TimeUnit timeoutUnit) throws InterruptedException {
        return (T) waitFor(p -> p.getType() == type, timeout, timeoutUnit);
    }

    /**
     * This blocks the current thread until the next packet of the given type is received.
     *
     * @param type The type of packet to wait for.
     * @return The packet which caused the blocking to end.
     *
     * @throws InterruptedException
     */
    default <T extends Packet> T waitForNext(PacketType type) {
        try {
            return waitForNext(type, -1, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return null; //This should never happen
        }
    }

    /**
     * This facilitates a packet exchange (sends a packet, then immediately waits for its response).
     *
     * @param sending The packet to send.
     * @param selector The predicate to determine the packet expected to be returned.
     * @param timeout The amount of time to wait before forcibly interrupting the blocking.
     * @param timeoutUnit The unit which the timeout represents.
     * @return The response from the server.
     *
     * @throws InterruptedException
     */
    default Packet exchange(Packet sending, Predicate<Packet> selector, long timeout, TimeUnit timeoutUnit) throws InterruptedException {
        return Support.waitFor(this, selector, timeout, timeoutUnit, () -> send(sending));
    }

    /**
     * This facilitates a packet exchange (sends a packet, then immediately waits for its response).
     *
     * @param sending The packet to send.
     * @param selector The predicate to determine the packet expected to be returned.
     * @return The response from the server.
     */
    default Packet exchange(Packet sending, Predicate<Packet> selector) {
        try {
            return exchange(sending, selector, -1, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return null; //This should never happen
        }
    }

    /**
     * This facilitates a packet exchange (sends a packet, then immediately waits for its response).
     *
     * @param sending The packet to send.
     * @param type The type of packet expected to receive in return.
     * @param timeout The amount of time to wait before forcibly interrupting the blocking.
     * @param timeoutUnit The unit which the timeout represents.
     * @return The response from the server.
     *
     * @throws InterruptedException
     */
    default <T extends Packet> T exchange(Packet sending, PacketType type, long timeout, TimeUnit timeoutUnit) throws InterruptedException {
        return (T) exchange(sending, p -> p.getType() == type, timeout, timeoutUnit);
    }

    /**
     * This facilitates a packet exchange (sends a packet, then immediately waits for its response).
     *
     * @param sending The packet to send.
     * @param type The type of packet expected to receive in return.
     * @return The response from the server.
     */
    default <T extends Packet> T exchange(Packet sending, PacketType type) {
        return (T) exchange(sending, p -> p.getType() == type);
    }

    /**
     * This attempts to determine the latency between one side to the other.
     *
     * @return The latency in ms.
     */
    default long pollPing() {
        PingPacket pingPacket = new PingPacket();
        PongPacket pongPacket = (PongPacket) exchange(pingPacket, packet -> packet.getType() == PacketType.PONG && ((PongPacket) packet).getIdentifier() == pingPacket.getIdentifier());
        return System.currentTimeMillis() - pingPacket.getSendTime();
    }

    /**
     * Signals for a clean close of the connection.
     *
     * @param exitCode The exit code to stop the connection with (non-zero is an error by convention).
     *
     * @throws IOException
     */
    default void cleanlyClose(int exitCode) throws IOException {
        send(new ConnectionKilledPacket(exitCode));
        close();
    }

    /**
     * Checks if the handler has been closed.
     *
     * @return True if closed, false if otherwise.
     */
    boolean isClosed();
}

final class Support { //Because no private methods in java 8

    static final Runnable NO_OP_RUNNABLE = () -> {};

    static Packet waitFor(PacketHandler handler, Predicate<Packet> packetPredicate, long timeout, TimeUnit timeoutUnit,
                          Runnable hook) throws InterruptedException {
        SynchronousQueue<Packet> result = new SynchronousQueue<>();

        handler.temporarilyHandle(p -> {
            if (packetPredicate.test(p)) {
                result.offer(p);
                return true;
            } else {
                return false;
            }
        });

        hook.run();

        return timeout < 0 ? result.poll() : result.poll(timeout, timeoutUnit);
    }
}
