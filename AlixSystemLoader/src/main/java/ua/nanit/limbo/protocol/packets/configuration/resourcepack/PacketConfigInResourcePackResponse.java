package ua.nanit.limbo.protocol.packets.configuration.resourcepack;

import alix.common.utils.netty.safety.NettySafety;
import com.github.retrooper.packetevents.wrapper.configuration.client.WrapperConfigClientResourcePackStatus;
import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.packets.retrooper.InRetrooperPacket;
import ua.nanit.limbo.server.LimboServer;
import ua.nanit.limbo.server.Log;

public final class PacketConfigInResourcePackResponse extends InRetrooperPacket<WrapperConfigClientResourcePackStatus> {

    public PacketConfigInResourcePackResponse() {
        super(WrapperConfigClientResourcePackStatus.class);
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        if (!NanoLimbo.enableFingerprinting)
            throw NettySafety.INVALID_PACKET;

        Log.error("result=" + this.wrapper().getResult());
        //conn.finishConfig();
    }
}