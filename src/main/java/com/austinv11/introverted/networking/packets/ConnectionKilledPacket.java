package com.austinv11.introverted.networking.packets;

import com.austinv11.introverted.common.Introverted;
import com.austinv11.introverted.mapping.Serialized;
import com.austinv11.introverted.networking.Packet;
import com.austinv11.introverted.networking.PacketType;

/**
 * This packet is sent by either a client or server in order to indicate that the connection should be terminated.
 */
public class ConnectionKilledPacket extends Packet {

    @Serialized
    private final int exitCode;

    ConnectionKilledPacket() {
        super();
        exitCode = 0;
    }

    public ConnectionKilledPacket(int exitCode) {
        super(Introverted.VERSION, PacketType.CONNECTION_KILLED);
        this.exitCode = exitCode;
    }

    /**
     * The arbitrary exit code.
     *
     * @return The exit code, by convention a non-zero exit code represents and abnormal exit.
     */
    public int getExitCode() {
        return exitCode;
    }
}
