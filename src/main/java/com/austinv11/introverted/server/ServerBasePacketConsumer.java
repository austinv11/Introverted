package com.austinv11.introverted.server;

import com.austinv11.introverted.common.BasePacketConsumer;
import com.austinv11.introverted.networking.Packet;
import com.austinv11.introverted.networking.PacketHandler;

public class ServerBasePacketConsumer extends BasePacketConsumer {

    public ServerBasePacketConsumer(PacketHandler handler) {
        super(handler);
    }

    @Override
    public void handle(Packet packet) {

    }
}
