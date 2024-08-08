package shadow.utils.misc.packet.constructors;


import com.github.retrooper.packetevents.protocol.sound.Sound;
import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import io.netty.buffer.ByteBuf;
import shadow.utils.netty.NettyUtils;
import shadow.utils.world.AlixWorld;

public final class OutSoundPacketConstructor {

    public static ByteBuf constructConstUnverified(Sound sound) {
        return NettyUtils.constBuffer(new WrapperPlayServerSoundEffect(sound, SoundCategory.MASTER, AlixWorld.TELEPORT_VEC3I, 10, 1));
    }

/*    public static ByteBuf constructConstStopSound(Sound sound) {
        return NettyUtils.constBuffer(new WrapperPlayServerSoundEffect(sound, SoundCategory.MASTER, AlixWorld.TELEPORT_VEC3I, 10, 1));
    }*/

    private OutSoundPacketConstructor() {
    }
}