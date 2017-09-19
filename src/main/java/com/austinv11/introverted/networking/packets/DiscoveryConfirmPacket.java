package com.austinv11.introverted.networking.packets;

import com.austinv11.introverted.common.Introverted;
import com.austinv11.introverted.mapping.Serialized;
import com.austinv11.introverted.networking.Packet;
import com.austinv11.introverted.networking.PacketType;

public class DiscoveryConfirmPacket extends Packet {

    @Serialized
    private final String platform;

    DiscoveryConfirmPacket() {
        super();
        platform = null;
    }

    public DiscoveryConfirmPacket(String platform) {
        super(Introverted.VERSION, PacketType.DISCOVERY_CONFIRM);
        this.platform = platform;
    }

    public String getPlatformIdentifier() {
        return platform;
    }
}
