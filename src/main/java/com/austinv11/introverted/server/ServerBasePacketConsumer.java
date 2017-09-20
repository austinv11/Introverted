package com.austinv11.introverted.server;

import com.austinv11.introverted.common.BasePacketConsumer;
import com.austinv11.introverted.networking.Packet;
import com.austinv11.introverted.networking.PacketHandler;
import com.austinv11.introverted.networking.packets.DiscoveryConfirmPacket;

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
        }
    }
}
