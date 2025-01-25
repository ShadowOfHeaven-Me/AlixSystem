package ua.nanit.limbo.protocol.packets.play.animation;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

public final class PacketPlayOutAnimation extends OutRetrooperPacket<WrapperPlayServerEntityAnimation> {

    public PacketPlayOutAnimation() {
        super(WrapperPlayServerEntityAnimation.class);
    }

    public PacketPlayOutAnimation setEntityId(int entityID) {
        this.wrapper().setEntityId(entityID);
        return this;
    }

    public PacketPlayOutAnimation setType(WrapperPlayServerEntityAnimation.EntityAnimationType type) {
        this.wrapper().setType(type);
        return this;
    }
}