package ua.nanit.limbo.server.data;

import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.play.PacketTitleLegacy;
import ua.nanit.limbo.protocol.packets.play.PacketTitleSetSubTitle;
import ua.nanit.limbo.protocol.packets.play.PacketTitleSetTitle;
import ua.nanit.limbo.protocol.packets.play.PacketTitleTimes;
import ua.nanit.limbo.protocol.registry.Version;

import static com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTitle.TitleAction.*;

public final class TitlePacketSnapshot {

    private final PacketSnapshot title;
    private final PacketSnapshot subtitle;
    private final PacketSnapshot times;

    private final PacketSnapshot legacyTitle;
    private final PacketSnapshot legacySubtitle;
    private final PacketSnapshot legacyTimes;

    public TitlePacketSnapshot(Title title) {
        this.title = PacketSnapshot.of(new PacketTitleSetTitle().setTitle(title.getTitle()));
        this.subtitle = PacketSnapshot.of(new PacketTitleSetSubTitle().setSubtitle(title.getSubtitle()));
        this.times = PacketSnapshot.of(new PacketTitleTimes().setFadeIn(title.getFadeIn()).setStay(title.getStay()).setFadeOut(title.getFadeOut()));

        this.legacyTitle = PacketSnapshot.of(new PacketTitleLegacy().setTitle(title).setAction(SET_TITLE));
        this.legacySubtitle = PacketSnapshot.of(new PacketTitleLegacy().setTitle(title).setAction(SET_SUBTITLE));
        this.legacyTimes = PacketSnapshot.of(new PacketTitleLegacy().setTitle(title).setAction(SET_TIMES_AND_DISPLAY));
    }

    public void write(ClientConnection connection) {
        var version = connection.getClientVersion();
        if (version.lessOrEqual(Version.V1_7_6)) return;

        if (version.moreOrEqual(Version.V1_17)) {
            connection.writePacket(title);
            connection.writePacket(subtitle);
            connection.writePacket(times);
        } else {
            connection.writePacket(legacyTitle);
            connection.writePacket(legacySubtitle);
            connection.writePacket(legacyTimes);
        }
    }
}