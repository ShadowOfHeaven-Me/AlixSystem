package ua.nanit.limbo.protocol.packets.play;

import alix.common.packets.message.MessageWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetTitleText;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

public class PacketTitleSetTitle extends OutRetrooperPacket<WrapperPlayServerSetTitleText> {

    public PacketTitleSetTitle() {
        super(WrapperPlayServerSetTitleText.class);
    }

    //Was Component.text(title) - see PacketPlayOutDisconnect#setReason(String) for why that breaks hex codes.
    public PacketTitleSetTitle setTitle(String title) {
        this.wrapper().setTitle(MessageWrapper.parseLegacy(title));
        return this;
    }
}
