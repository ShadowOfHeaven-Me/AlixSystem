/*
package shadow.utils.objects.filter.packet.check.impl;

import net.minecraft.network.protocol.game.*;
import shadow.utils.objects.filter.packet.check.PacketAffirmator;

public class OptimizedPacketAffirmatorImpl implements PacketAffirmator {

    private static final Class<?> serverboundChatCommandPacket;
    private static final boolean isServerboundChatCommandPacketPresent;

    static {
        Class<?> serverboundChatCommandPacket0;
        try {
            serverboundChatCommandPacket0 = Class.forName("net.minecraft.network.protocol.game.ServerboundChatCommandPacket");
        } catch (ClassNotFoundException e) {//ignored
            serverboundChatCommandPacket0 = null;
        }
        serverboundChatCommandPacket = serverboundChatCommandPacket0;
        isServerboundChatCommandPacketPresent = serverboundChatCommandPacket != null;
    }

    @Override
    public boolean isWindowOrKeepAlive(Object packet) {
        return packet instanceof PacketPlayInWindowClick || packet instanceof PacketPlayInCloseWindow || packet instanceof PacketPlayInKeepAlive;
    }

    @Override
    public boolean isCommandOrKeepAlive(Object packet) {
        if (packet instanceof PacketPlayInKeepAlive) return true;
        return isServerboundChatCommandPacketPresent ? serverboundChatCommandPacket.isInstance(packet) : packet instanceof PacketPlayInClientCommand;
    }

    @Override
    public boolean isKeepAlive(Object packet) {
        return packet instanceof PacketPlayInKeepAlive;
    }

    @Override
    public boolean isCommand(Object packet) {
        return isServerboundChatCommandPacketPresent ? serverboundChatCommandPacket.isInstance(packet) : packet instanceof PacketPlayInClientCommand;
    }

    @Override
    public boolean isMove(Object packet) {
        return packet instanceof PacketPlayInFlying;
    }

    @Override
    public boolean isFromServerInfoCaptchaForbidden(Object packet) {
        switch (packet.getClass().getSimpleName()) {
            case "PacketPlayOutRelEntityMove":
            case "PacketPlayOutNamedEntitySpawn":
            case "PacketPlayOutSpawnEntityLiving":
            case "PacketPlayOutSpawnEntity":
            case "PacketPlayOutEntityMetadata":
            case "PacketPlayOutEntityEquipment":
            case "PacketPlayOutEntityHeadRotation":
            case "PacketPlayOutEntityStatus":
                //case "PacketPlayOutEntityEffect":
                //case "PacketPlayOutEntityAttach":
            case "PacketPlayOutEntityVelocity":
            case "PacketPlayOutEntityDestroy":
            case "PacketPlayOutEntityLook":
            case "PacketPlayOutPlayerInfo":
            case "ClientboundPlayerInfoUpdatePacket":
            case "ClientboundBundlePacket":
                //case "PacketPlayOutRespawn":
                //case "ClientboundLevelChunkWithLightPacket":
                return true;
        }
        return false;
    }

    @Override
    public boolean isKeepAlive(String name) {
        return false;
    }

    @Override
    public boolean isCommand(String name) {
        return false;
    }

    @Override
    public boolean isMove(String name) {
        return false;
    }
}*/
