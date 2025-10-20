package ua.nanit.limbo.protocol.packets.play.tick;

import alix.common.utils.netty.WrapperUtils;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClientTickEnd;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketIn;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.LimboServer;

public final class PacketPlayInTickEnd implements PacketIn {

    public static final PacketPlayInTickEnd INSTANCE = new PacketPlayInTickEnd();
    public static final WrapperPlayClientClientTickEnd WRAPPER = WrapperUtils.allocEmpty(WrapperPlayClientClientTickEnd.class);

    @Override
    public void decode(ByteMessage msg, Version version) {
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        conn.getVerifyState().handle(this);
    }

    private PacketPlayInTickEnd() {
    }
}