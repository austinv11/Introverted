package com.austinv11.introverted.networking;

import com.austinv11.introverted.networking.packets.PingPacket;
import com.austinv11.introverted.networking.packets.PongPacket;

import java.io.Closeable;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface PacketHandler extends Closeable {

    void send(Packet packet);

    default <T extends Packet> void handle(PacketType packetType, Consumer<T> packetHandler) {
        handle(p -> {
            if (p.getType() == packetType)
                packetHandler.accept((T) p);
        });
    }

    void handle(Consumer<Packet> packetConsumer);

    void unregisterPacketConsumer(Consumer<Packet> packetConsumer);

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

    default Packet waitFor(Predicate<Packet> packetPredicate) {
        try {
            return waitFor(packetPredicate, -1, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return null; //Should never happen
        }
    }

    default Packet waitFor(Predicate<Packet> packetPredicate, long timeout, TimeUnit timeoutUnit) throws InterruptedException {
        SynchronousQueue<Packet> result = new SynchronousQueue<>();
        temporarilyHandle(p -> {
            if (packetPredicate.test(p)) {
                result.offer(p);
                return true;
            } else {
                return false;
            }
        });
        return timeout < 0 ? result.poll() : result.poll(timeout, timeoutUnit);
    }

    default <T extends Packet> T waitForNext(PacketType type, long timeout, TimeUnit timeoutUnit) throws InterruptedException {
        return (T) waitFor(p -> p.getType() == type, timeout, timeoutUnit);
    }

    default <T extends Packet> T waitForNext(PacketType type) {
        try {
            return waitForNext(type, -1, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return null; //This should never happen
        }
    }

    default long pollPing() {
        PingPacket pingPacket = new PingPacket();
        send(pingPacket);
        PongPacket pongPacket = (PongPacket) waitFor(packet -> packet.getType() == PacketType.PONG && ((PongPacket) packet).getIdentifier() == pingPacket.getIdentifier());
        return System.currentTimeMillis() - pingPacket.getSendTime();
    }

    boolean isClosed();
}
