package ua.nanit.limbo.protocol.packets.play.inventory;

import alix.common.packets.inventory.AlixInventoryType;
import alix.common.packets.inventory.InventoryWrapper;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import net.kyori.adventure.text.Component;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.snapshot.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;
import ua.nanit.limbo.protocol.registry.Version;

public final class PacketPlayOutInventoryOpen extends OutRetrooperPacket<WrapperPlayServerOpenWindow> {

    private final AlixInventoryType type;

    public PacketPlayOutInventoryOpen() {
        super(WrapperPlayServerOpenWindow.class);
        this.type = null;
    }

    public PacketPlayOutInventoryOpen(WrapperPlayServerOpenWindow wrapper, AlixInventoryType type) {
        super(wrapper);
        this.type = type;
    }

    @Override
    public void encode(ByteMessage msg, Version version) {
        this.wrapper().setType(this.type.getId(version.getRetrooperVersion().toClientVersion()));
        super.encode(msg, version);
    }

    public static PacketSnapshot snapshot(AlixInventoryType type, String title) {
        return create(type, title).toSnapshot();
    }

    public static PacketPlayOutInventoryOpen create(AlixInventoryType type, String title) {
        return new PacketPlayOutInventoryOpen(InventoryWrapper.createInvOpen(type, Component.text(title), ClientVersion.getLatest()), type);
    }
}