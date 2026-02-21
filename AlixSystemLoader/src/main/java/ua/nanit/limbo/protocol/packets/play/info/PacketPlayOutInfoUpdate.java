package ua.nanit.limbo.protocol.packets.play.info;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

import java.util.EnumSet;
import java.util.List;

public final class PacketPlayOutInfoUpdate extends OutRetrooperPacket<WrapperPlayServerPlayerInfoUpdate> {

    public PacketPlayOutInfoUpdate() {
        super(WrapperPlayServerPlayerInfoUpdate.class);
    }

    public static PacketPlayOutInfoUpdate of(EnumSet<WrapperPlayServerPlayerInfoUpdate.Action> actions, List<WrapperPlayServerPlayerInfoUpdate.PlayerInfo> entries) {
        var packet = new PacketPlayOutInfoUpdate();
        packet.wrapper().setActions(actions);
        packet.wrapper().setEntries(entries);
        return packet;
    }
}