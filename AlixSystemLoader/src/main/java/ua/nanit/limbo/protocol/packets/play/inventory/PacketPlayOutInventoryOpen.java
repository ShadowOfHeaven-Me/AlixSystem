package ua.nanit.limbo.protocol.packets.play.inventory;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

public final class PacketPlayOutInventoryOpen extends OutRetrooperPacket<WrapperPlayServerOpenWindow> {

    public PacketPlayOutInventoryOpen() {
        super(WrapperPlayServerOpenWindow.class);
    }

    public PacketPlayOutInventoryOpen(WrapperPlayServerOpenWindow wrapper) {
        super(wrapper);
    }
}