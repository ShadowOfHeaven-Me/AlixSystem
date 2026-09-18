package ua.nanit.limbo.protocol.packets.login;

import alix.common.packets.message.MessageWrapper;
import com.github.retrooper.packetevents.wrapper.configuration.server.WrapperConfigServerDisconnect;
import ua.nanit.limbo.protocol.snapshot.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

public class PacketConfigDisconnect extends OutRetrooperPacket<WrapperConfigServerDisconnect> {

    public PacketConfigDisconnect() {
        super(WrapperConfigServerDisconnect.class);
    }

    //Was Component.text(reason) - see PacketPlayOutDisconnect#setReason(String) for why that breaks hex codes.
    public PacketConfigDisconnect setReason(String reason) {
        this.wrapper().setReason(MessageWrapper.parseLegacy(reason));
        return this;
    }

    public static PacketSnapshot snapshot(String reason) {
        return new PacketConfigDisconnect().setReason(reason).toSnapshot();
    }
}
