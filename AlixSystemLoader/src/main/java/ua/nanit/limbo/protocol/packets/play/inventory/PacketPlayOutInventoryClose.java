package ua.nanit.limbo.protocol.packets.play.inventory;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCloseWindow;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

public final class PacketPlayOutInventoryClose extends OutRetrooperPacket<WrapperPlayServerCloseWindow> {

    public PacketPlayOutInventoryClose() {
        super(WrapperPlayServerCloseWindow.class);
    }

    public PacketPlayOutInventoryClose(WrapperPlayServerCloseWindow wrapper) {
        super(wrapper);
    }

    public PacketPlayOutInventoryClose(int id) {
        this();
        this.wrapper().setWindowId(id);
    }
}