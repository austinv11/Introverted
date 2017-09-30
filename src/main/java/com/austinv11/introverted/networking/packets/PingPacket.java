package com.austinv11.introverted.networking.packets;

import com.austinv11.introverted.common.Introverted;
import com.austinv11.introverted.mapping.Serialized;
import com.austinv11.introverted.networking.Packet;
import com.austinv11.introverted.networking.PacketType;
import com.austinv11.introverted.networking.TraceablePacket;

/**
 * This is sent by any side in order to ensure that a connection is still alive.
 */
public class PingPacket extends TraceablePacket {

    public PingPacket() {
        super(Introverted.VERSION, PacketType.PING, System.currentTimeMillis());
    }

    /**
     * This gets the time the ping was sent in epoch milliseconds.
     *
     * @return The send time.
     */
    public long getSendTime() {
        return getId(); //Since the id is the timestamp
    }
}
