package com.austinv11.introverted.networking.packets;

import com.austinv11.introverted.common.Introverted;
import com.austinv11.introverted.mapping.Serialized;
import com.austinv11.introverted.networking.Packet;
import com.austinv11.introverted.networking.PacketType;
import com.austinv11.introverted.networking.TraceablePacket;

/**
 * This is sent by the server in response to a {@link DiscoveryPacket}.
 */
public class DiscoveryConfirmPacket extends TraceablePacket {

    @Serialized(0)
    private final String platform;

    DiscoveryConfirmPacket() {
        super();
        platform = null;
    }

    public DiscoveryConfirmPacket(String platform, long id) {
        super(Introverted.VERSION, PacketType.DISCOVERY_CONFIRM, id);
        this.platform = platform;
    }

    /**
     * The platform identifier for the server.
     *
     * @return The platform identifier (i.e. 'JVM-light').
     */
    public String getPlatformIdentifier() {
        return platform;
    }
}
