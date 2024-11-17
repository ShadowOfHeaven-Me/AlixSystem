package shadow.utils.misc.packet.constructors;


import alix.libs.com.github.retrooper.packetevents.protocol.sound.Sound;
import alix.libs.com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import alix.libs.com.github.retrooper.packetevents.protocol.world.Location;
import alix.libs.com.github.retrooper.packetevents.util.Vector3i;
import alix.libs.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import io.netty.buffer.ByteBuf;
import shadow.utils.netty.NettyUtils;
import shadow.utils.world.AlixWorld;

public final class OutSoundPacketConstructor {

    public static ByteBuf constructConstUnverified(Sound sound) {
        return NettyUtils.constBuffer(new WrapperPlayServerSoundEffect(sound, SoundCategory.MASTER, AlixWorld.TELEPORT_VEC3I, 1000, 1));
    }

    public static ByteBuf constructConst(Sound sound, Location loc) {
        return NettyUtils.constBuffer(new WrapperPlayServerSoundEffect(sound, SoundCategory.MASTER, loc.getPosition().toVector3i(), 1000, 1));
    }

    public static WrapperPlayServerSoundEffect construct(Sound sound, Vector3i loc) {
        return new WrapperPlayServerSoundEffect(sound, SoundCategory.MASTER, loc, 1000, 1);
    }
/*    public static ByteBuf constructConstStopSound(Sound sound) {
        return NettyUtils.constBuffer(new WrapperPlayServerSoundEffect(sound, SoundCategory.MASTER, AlixWorld.TELEPORT_VEC3I, 10, 1));
    }*/

    private OutSoundPacketConstructor() {
    }
}