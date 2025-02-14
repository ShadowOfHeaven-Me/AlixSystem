package ua.nanit.limbo.protocol.packets.play.sound;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

public final class PacketPlayOutSound extends OutRetrooperPacket<WrapperPlayServerSoundEffect> {

    public PacketPlayOutSound() {
        super(WrapperPlayServerSoundEffect.class);
    }

    public PacketPlayOutSound(WrapperPlayServerSoundEffect wrapper) {
        super(wrapper);
    }
}