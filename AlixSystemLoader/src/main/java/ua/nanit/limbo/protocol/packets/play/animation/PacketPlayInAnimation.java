package ua.nanit.limbo.protocol.packets.play.animation;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAnimation;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketIn;
import ua.nanit.limbo.protocol.packets.retrooper.InRetrooperPacket;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.LimboServer;

public final class PacketPlayInAnimation implements PacketIn {

    /*private int entityId = -1;
    private int hand = MAIN_HAND;
    private LegacyAnimationType type = LegacyAnimationType.SWING_ARM;*/

    public PacketPlayInAnimation() {
    }

    @Override
    public void decode(ByteMessage msg, Version version) {
//        if (version.less(Version.V1_8)) {
//            entityId = msg.readInt();
//            type = LegacyAnimationType.getById(msg.readByte());
//        } else if (version.moreOrEqual(Version.V26_3)) {
//            hand = MAIN_HAND;
//        } else if (version.more(Version.V1_8)) {
//            // Only 1.9+ clients have an offhand
//            hand = ProtocolUtil.readVarInt(byteBuf);
//        }
    }

    /*@Override
    public void handle(ClientConnection conn, LimboServer server) {
        conn.getVerifyState().handle(this);
    }*/
}