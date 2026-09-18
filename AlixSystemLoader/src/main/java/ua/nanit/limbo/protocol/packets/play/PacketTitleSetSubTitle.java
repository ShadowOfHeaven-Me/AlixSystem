package ua.nanit.limbo.protocol.packets.play;

import alix.common.packets.message.MessageWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetTitleSubtitle;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

public class PacketTitleSetSubTitle extends OutRetrooperPacket<WrapperPlayServerSetTitleSubtitle> {

    public PacketTitleSetSubTitle() {
        super(WrapperPlayServerSetTitleSubtitle.class);
    }

    //Was Component.text(title) - see PacketPlayOutDisconnect#setReason(String) for why that breaks hex codes.
    public PacketTitleSetSubTitle setSubtitle(String title) {
        this.wrapper().setSubtitle(MessageWrapper.parseLegacy(title));
        return this;
    }
}
