/*
package ua.nanit.limbo.connection.captcha.blocks;

import alix.common.utils.AlixCommonUtils;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.captcha.CaptchaBlock;
import ua.nanit.limbo.protocol.packets.play.blocks.PacketPlayOutBlockUpdate;
import ua.nanit.limbo.protocol.registry.Version;

import static ua.nanit.limbo.connection.captcha.CaptchaBlock.*;
import static ua.nanit.limbo.protocol.registry.Version.*;

public final class CaptchaBlockCheck {

    */
/*static {
        //CaptchaBlock block = COBBLESTONE_WALL;//AlixCommonUtils.getRandom(CaptchaBlock.values(), getLen(clientVersion));

        //Log.error("CHOSEN: " + block.getBlockType().getName() + " MAT: " + block.getBlockType().getMaterialType().name());

        //PacketPlayOutBlockUpdate blockUpdate = new PacketPlayOutBlockUpdate().setType(block.getBlockType()).setPosition(new Vector3i()).setTransformer(block.getTransformer());
        //blockUpdate.toSnapshot();
    }*//*


    public static CaptchaBlock sendRandomBlock(ClientConnection connection) {
        Version clientVersion = connection.getClientVersion();
        CaptchaBlock block = AlixCommonUtils.getRandom(CaptchaBlock.values(), getLen(clientVersion));

        //Log.error("CHOSEN: " + block.getBlockType().getName() + " MAT: " + block.getBlockType().getMaterialType().name());

        PacketPlayOutBlockUpdate blockUpdate = new PacketPlayOutBlockUpdate()
                .setType(block.getBlockType())
                .setPosition(pos)
                .setTransformer(block.getTransformer());
        //PacketPlayOutBlockSectionUpdate blockUpdate = PacketPlayOutBlockSectionUpdate.packetToSpoof(pos, block.getBlockType(), connection.getClientVersion().getRetrooperVersion().toClientVersion());

        connection.writeAndFlushPacket(blockUpdate);
        return block;
    }

    private static int getLen(Version version) {
        if (version.less(V1_13)) return LEN_TILL_v1_13;
        if (version.less(V1_14)) return LEN_TILL_v1_14;
        if (version.less(V1_17)) return LEN_TILL_v1_17;

        return FULL_LEN;
    }
}*/
