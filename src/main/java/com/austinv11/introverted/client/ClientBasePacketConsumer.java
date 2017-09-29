package com.austinv11.introverted.client;

import com.austinv11.introverted.common.BasePacketConsumer;
import com.austinv11.introverted.networking.Packet;
import com.austinv11.introverted.networking.PacketHandler;

/**
 * {@link BasePacketConsumer} implementation for client side operations.
 */
public class ClientBasePacketConsumer extends BasePacketConsumer {

    public ClientBasePacketConsumer(PacketHandler handler) {
        super(handler);
    }

    @Override
    public void handle(Packet packet) {

    }
}
