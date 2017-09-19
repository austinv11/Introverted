package com.austinv11.introverted.networking;

import com.austinv11.introverted.networking.packets.PingPacket;
import com.austinv11.introverted.networking.packets.PongPacket;

import java.util.concurrent.SynchronousQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface PacketHandler {

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
        SynchronousQueue<Packet> result = new SynchronousQueue<>();
        temporarilyHandle(p -> {
            if (packetPredicate.test(p)) {
                result.offer(p);
                return true;
            } else {
                return false;
            }
        });
        return result.poll();
    }

    default <T extends Packet> T waitForNext(PacketType type) {
        return (T) waitFor(p -> p.getType() == type);
    }

    default long pollPing() {
        PingPacket pingPacket = new PingPacket();
        send(pingPacket);
        PongPacket pongPacket = (PongPacket) waitFor(packet -> packet.getType() == PacketType.PONG && ((PongPacket) packet).getIdentifier() == pingPacket.getIdentifier());
        return System.currentTimeMillis() - pingPacket.getSendTime();
    }
}
