package ua.nanit.limbo.connection.captcha.blocks;

import alix.common.utils.AlixCommonUtils;
import alix.libs.com.github.retrooper.packetevents.util.Vector3i;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.packets.play.blocks.PacketPlayOutBlockUpdate;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.Log;

import static ua.nanit.limbo.connection.captcha.blocks.CaptchaBlock.*;
import static ua.nanit.limbo.protocol.registry.Version.*;

public final class CaptchaBlockCheck {

    public static CaptchaBlock sendRandomBlock(ClientConnection connection, Vector3i pos) {
        Version clientVersion = connection.getClientVersion();
        CaptchaBlock block = AlixCommonUtils.getRandom(CaptchaBlock.values(), getLen(clientVersion));

        //Log.error("CHOSEN: " + block.getBlockType().getName() + " MAT: " + block.getBlockType().getMaterialType().name());

        PacketPlayOutBlockUpdate blockUpdate = new PacketPlayOutBlockUpdate().setType(clientVersion, block.getBlockType()).setPosition(pos);
        block.getTransformer().accept(blockUpdate.getState());
        Log.error("CHANNEL: " + connection.getChannel().pipeline().names());

        connection.getChannel().pipeline().removeFirst();
        Log.error("CHANNEL2: " + connection.getChannel().pipeline().names());

        connection.writeAndFlushPacket(blockUpdate);
        return block;
    }

    private static int getLen(Version version) {
        if (version.less(V1_13)) return LEN_TILL_v1_13;
        if (version.less(V1_14)) return LEN_TILL_v1_14;
        if (version.less(V1_17)) return LEN_TILL_v1_17;

        return FULL_LEN;
    }
}