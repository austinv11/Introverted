package com.austinv11.introverted.server;

import com.austinv11.introverted.common.BasePacketConsumer;
import com.austinv11.introverted.common.Introverted;
import com.austinv11.introverted.networking.Packet;
import com.austinv11.introverted.networking.PacketHandler;
import com.austinv11.introverted.networking.packets.DiscoveryConfirmPacket;
import com.austinv11.introverted.networking.packets.HandshakeConfirmPacket;
import com.austinv11.introverted.networking.packets.HandshakePacket;
import com.austinv11.introverted.networking.packets.HandshakeRefusePacket;

/**
 * {@link BasePacketConsumer} implementation for server side operations.
 */
public class ServerBasePacketConsumer extends BasePacketConsumer {

    public ServerBasePacketConsumer(PacketHandler handler) {
        super(handler);
    }

    @Override
    public void handle(Packet packet) {
        switch (packet.getType()) {
            case DISCOVERY:
                getHandler().send(new DiscoveryConfirmPacket(IntrovertedServer.JVM_LIGHT_PLATFORM));
                break;
            case HANDSHAKE:
                HandshakePacket handshakePacket = (HandshakePacket) packet;
                if (handshakePacket.getProtocolVersion() == Introverted.VERSION)
                    getHandler().send(new HandshakeConfirmPacket());
                else
                    getHandler().send(new HandshakeRefusePacket(
                            String.format("Incompatible client version (expected %s, got %s)", Introverted.VERSION, packet.getProtocolVersion())));
                break;
        }
    }
}
