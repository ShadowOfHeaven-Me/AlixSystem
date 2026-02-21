package ua.nanit.limbo.protocol.packets.play.disconnect;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisconnect;
import net.kyori.adventure.text.Component;
import ua.nanit.limbo.protocol.snapshot.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

public final class PacketPlayOutDisconnect extends OutRetrooperPacket<WrapperPlayServerDisconnect> {

    public PacketPlayOutDisconnect() {
        super(WrapperPlayServerDisconnect.class);
    }

    public PacketPlayOutDisconnect setReason(String reason) {
        this.wrapper().setReason(Component.text(reason));
        return this;
    }

    public static PacketSnapshot error(String reason) {
        return snapshot("§c" + reason);
    }

    public static PacketSnapshot snapshot(String reason) {
        return new PacketPlayOutDisconnect().setReason(reason).toSnapshot();
    }
}