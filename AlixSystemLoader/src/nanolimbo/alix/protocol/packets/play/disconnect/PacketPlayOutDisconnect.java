package nanolimbo.alix.protocol.packets.play.disconnect;

import alix.libs.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisconnect;
import nanolimbo.alix.protocol.PacketSnapshot;
import nanolimbo.alix.protocol.packets.retrooper.OutRetrooperPacket;
import net.kyori.adventure.text.Component;

public final class PacketPlayOutDisconnect extends OutRetrooperPacket<WrapperPlayServerDisconnect> {

    public PacketPlayOutDisconnect() {
        super(WrapperPlayServerDisconnect.class);
    }

    public PacketPlayOutDisconnect setReason(String reason) {
        this.wrapper().setReason(Component.text(reason));
        return this;
    }

    public static PacketSnapshot error(String reason) {
        return new PacketPlayOutDisconnect().setReason("§c[Alix] \n" + reason).toSnapshot();
    }
}