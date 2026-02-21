package ua.nanit.limbo.connection.captcha.arm;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;
import ua.nanit.limbo.protocol.snapshot.PacketSnapshot;
import ua.nanit.limbo.protocol.snapshot.PacketSnapshots;
import ua.nanit.limbo.protocol.packets.play.animation.PacketPlayOutAnimation;

public final class ArmAnimations {

    public static final PacketSnapshot SELF_SWING = ofId(PacketSnapshots.PLAYER_ENTITY_ID);

    private static PacketSnapshot ofId(int entityId) {
        return PacketSnapshot.of(new PacketPlayOutAnimation().setEntityId(entityId).setType(WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_MAIN_ARM));
    }
}