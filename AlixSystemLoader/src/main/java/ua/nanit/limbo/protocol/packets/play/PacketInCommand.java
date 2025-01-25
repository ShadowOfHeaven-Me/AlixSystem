package ua.nanit.limbo.protocol.packets.play;

import alix.common.utils.netty.WrapperUtils;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommand;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketIn;
import ua.nanit.limbo.protocol.registry.Version;

public final class PacketInCommand implements PacketIn {

    private WrapperPlayClientChatCommand wrapper;

    @Override
    public void decode(ByteMessage msg, Version version) {
        this.wrapper = WrapperUtils.readNoID(msg.getBuf(), version.getRetrooperVersion(), WrapperPlayClientChatCommand.class);
    }

    /*@Override
    public void handle(ClientConnection conn, LimboServer server) {
        server.getCommandHandler().handleCommand(conn, this.wrapper.getCommand());
    }*/
}