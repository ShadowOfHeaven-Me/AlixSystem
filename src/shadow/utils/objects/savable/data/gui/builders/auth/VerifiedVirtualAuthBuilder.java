package shadow.utils.objects.savable.data.gui.builders.auth;

import alix.common.messages.Messages;
import alix.libs.com.github.retrooper.packetevents.protocol.sound.Sound;
import alix.libs.com.github.retrooper.packetevents.protocol.sound.Sounds;
import alix.libs.com.github.retrooper.packetevents.util.Vector3i;
import alix.libs.io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.netty.buffer.ByteBuf;
import shadow.utils.misc.packet.constructors.OutMessagePacketConstructor;
import shadow.utils.misc.packet.constructors.OutSoundPacketConstructor;
import shadow.utils.users.types.VerifiedUser;

import java.util.function.Consumer;

public final class VerifiedVirtualAuthBuilder extends VirtualAuthBuilder {

    private final VerifiedUser user;
    private final Vector3i loc;

    public VerifiedVirtualAuthBuilder(VerifiedUser user, Consumer<Boolean> onConfirm) {
        super(user, onConfirm, false);
        this.user = user;
        this.loc = vec3iLoc(user);
    }

    @Override
    void playSoundOnSuccess() {
        this.send(Sounds.ENTITY_PLAYER_LEVELUP);
    }

    @Override
    void playSoundOnDenial() {
        this.send(Sounds.ENTITY_VILLAGER_NO);
    }

    @Override
    void playSoundOnDigitAppend() {
        this.send(Sounds.BLOCK_NOTE_BLOCK_HARP);
    }

    @Override
    void playSoundOnLastRemove() {
        this.send(Sounds.BLOCK_NOTE_BLOCK_SNARE);
    }

    @Override
    void playSoundOnAllReset() {
        this.send(Sounds.ENTITY_ITEM_BREAK);
    }

    private void send(Sound sound) {
        //Vector3i vec3i = SpigotConversionUtil.fromBukkitLocation(this.user.getPlayer().getLocation()).getPosition().toVector3i();
        //Main.logError("LOCCCCCCC " + this.loc + " VEC " + vec3i);
        send(sound, this.user, this.loc);
    }

    public static void send(Sound sound, VerifiedUser user, Vector3i loc) {
        user.writeAndFlushDynamicSilently(OutSoundPacketConstructor.construct(sound, loc));
    }

    private static final ByteBuf
            accessConfirmedMessagePacket = OutMessagePacketConstructor.constructConst(Messages.getWithPrefix("google-auth-access-confirmed")),
            accessDeniedMessagePacket = OutMessagePacketConstructor.constructConst(Messages.getWithPrefix("google-auth-access-confirmation-failed"));

    public static void visualsOnProvenAccess(VerifiedUser user) {
        user.writeAndFlushConstSilently(accessConfirmedMessagePacket);

        send(Sounds.ENTITY_PLAYER_LEVELUP, user, vec3iLoc(user));
    }

    public static void visualsOnDeniedAccess(VerifiedUser user) {
        user.writeAndFlushConstSilently(accessDeniedMessagePacket);

        send(Sounds.ENTITY_ITEM_BREAK, user, vec3iLoc(user));
    }

    public static Vector3i vec3iLoc(VerifiedUser user) {
        return SpigotConversionUtil.fromBukkitLocation(user.getPlayer().getLocation()).getPosition().toVector3i();
    }
}