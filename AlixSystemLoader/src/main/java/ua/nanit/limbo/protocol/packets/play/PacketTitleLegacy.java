package ua.nanit.limbo.protocol.packets.play;

import alix.common.packets.message.MessageWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTitle;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;
import ua.nanit.limbo.server.data.Title;

public class PacketTitleLegacy extends OutRetrooperPacket<WrapperPlayServerTitle> {

    public PacketTitleLegacy() {
        super(WrapperPlayServerTitle.class);
    }

    public PacketTitleLegacy setAction(WrapperPlayServerTitle.TitleAction action) {
        this.wrapper().setAction(action);
        return this;
    }

    //Was Component.text(...) - see PacketPlayOutDisconnect#setReason(String) for why that breaks hex codes.
    public PacketTitleLegacy setTitle(Title title) {
        this.wrapper().setTitle(MessageWrapper.parseLegacy(title.getTitle()));
        this.wrapper().setSubtitle(MessageWrapper.parseLegacy(title.getSubtitle()));
        this.wrapper().setFadeInTicks(title.getFadeIn());
        this.wrapper().setStayTicks(title.getStay());
        this.wrapper().setFadeOutTicks(title.getFadeOut());
        return this;
    }
}
