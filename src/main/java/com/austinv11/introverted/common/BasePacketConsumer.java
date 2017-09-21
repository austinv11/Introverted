package com.austinv11.introverted.common;

import com.austinv11.introverted.networking.Packet;
import com.austinv11.introverted.networking.PacketHandler;
import com.austinv11.introverted.networking.PacketType;
import com.austinv11.introverted.networking.packets.PingPacket;
import com.austinv11.introverted.networking.packets.PongPacket;

import java.io.IOException;
import java.util.function.Consumer;

public abstract class BasePacketConsumer implements Consumer<Packet> {

    private final PacketHandler handler;

    public BasePacketConsumer(PacketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void accept(Packet packet) {
        //Handle common packet handling here
        if (packet.getType() == PacketType.PING)
            handler.send(new PongPacket(((PingPacket) packet).getIdentifier()));
        else if (packet.getType() == PacketType.CONNECTION_KILLED)
            try {
                handler.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        else //Delegate side specific handling here
            handle(packet);
    }

    public abstract void handle(Packet packet);

    public PacketHandler getHandler() {
        return handler;
    }
}
