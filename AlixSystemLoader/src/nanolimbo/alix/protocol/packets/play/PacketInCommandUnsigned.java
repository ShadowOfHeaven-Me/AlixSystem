package nanolimbo.alix.protocol.packets.play;

import alix.common.utils.netty.WrapperUtils;
import alix.libs.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommandUnsigned;
import nanolimbo.alix.connection.ClientConnection;
import nanolimbo.alix.protocol.ByteMessage;
import nanolimbo.alix.protocol.PacketIn;
import nanolimbo.alix.protocol.registry.Version;
import nanolimbo.alix.server.LimboServer;

public final class PacketInCommandUnsigned implements PacketIn {

    private WrapperPlayClientChatCommandUnsigned wrapper;

    @Override
    public void decode(ByteMessage msg, Version version) {
        this.wrapper = WrapperUtils.readNoID(msg.getBuf(), version.getRetrooperVersion(), WrapperPlayClientChatCommandUnsigned.class);
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        server.getCommandHandler().handleCommand(conn, this.wrapper.getCommand());
    }
}