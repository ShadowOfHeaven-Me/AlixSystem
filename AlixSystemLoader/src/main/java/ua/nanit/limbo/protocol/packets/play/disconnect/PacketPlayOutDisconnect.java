package ua.nanit.limbo.protocol.packets.play.disconnect;

import alix.common.packets.message.MessageWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisconnect;
import ua.nanit.limbo.protocol.snapshot.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

public final class PacketPlayOutDisconnect extends OutRetrooperPacket<WrapperPlayServerDisconnect> {

    public PacketPlayOutDisconnect() {
        super(WrapperPlayServerDisconnect.class);
    }

    //Was Component.text(reason) - that treats 'reason' (already legacy-color-translated by the caller, e.g.
    //via Messages.getWithPrefix()) as flat, unparsed text, so a "&#RRGGBB" hex code in it would show up as
    //literal '§x§...' characters instead of an actual color - parseLegacy() is the same hex-aware Adventure
    //legacy parser chat messages already use (see PacketPlayOutMessage#withMessage()).
    public PacketPlayOutDisconnect setReason(String reason) {
        this.wrapper().setReason(MessageWrapper.parseLegacy(reason));
        return this;
    }

    public static PacketSnapshot error(String reason) {
        return snapshot("§c" + reason);
    }

    public static PacketSnapshot snapshot(String reason) {
        return of(reason).toSnapshot();
    }

    public static PacketPlayOutDisconnect of(String reason) {
        return new PacketPlayOutDisconnect().setReason(reason);
    }
}