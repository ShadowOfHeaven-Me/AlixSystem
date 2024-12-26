package ua.nanit.limbo.protocol.packets.play.transfer;

import alix.libs.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTransfer;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

public final class PacketPlayOutTransfer extends OutRetrooperPacket<WrapperPlayServerTransfer> {

    public PacketPlayOutTransfer() {
        super(WrapperPlayServerTransfer.class);
    }

    public PacketPlayOutTransfer setHost(String host) {
        this.wrapper().setHost(host);
        return this;
    }

    public PacketPlayOutTransfer setPort(int port) {
        this.wrapper().setPort(port);
        return this;
    }
}