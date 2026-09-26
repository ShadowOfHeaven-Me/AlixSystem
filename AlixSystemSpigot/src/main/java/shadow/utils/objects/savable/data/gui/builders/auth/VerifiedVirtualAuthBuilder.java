package shadow.utils.objects.savable.data.gui.builders.auth;

import alix.common.messages.Messages;
import com.github.retrooper.packetevents.protocol.sound.Sound;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.util.Vector3i;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.netty.buffer.ByteBuf;
import shadow.Main;
import shadow.utils.misc.methods.MethodProvider;
import shadow.utils.misc.packet.constructors.OutDisconnectPacketConstructor;
import shadow.utils.misc.packet.constructors.OutMessagePacketConstructor;
import shadow.utils.misc.packet.constructors.OutSoundPacketConstructor;
import shadow.utils.users.types.VerifiedUser;

import java.util.function.Consumer;

public final class VerifiedVirtualAuthBuilder extends VirtualAuthBuilder {

    //FUNCTIONALITY (audit, 2026-09-24): this gate (Reset Token / View Recovery Codes / Show QR Code -
    //"prove your CURRENT app code before we show/change anything sensitive") is explicitly meant to defend
    //against someone briefly at an unattended, already-logged-in session (see GoogleAuthGUI's own comments)
    //- unlike every other code-entry path in the codebase (the login-time 2FA prompt via
    //UnverifiedVirtualAuthBuilder, and the email/recovery-code chat flows in LoginState), this one had no
    //attempt cap at all: a wrong guess just played a "denied" sound and let the player try again
    //immediately, unlimited times, against the live 6-digit code. Reuses max-auth-app-attempts for
    //consistency with the login-time gate, and kicks (rather than just locking the GUI) once exceeded,
    //since an attacker with unattended physical/client access who's already failed this many times is
    //exactly who this gate exists to keep out.
    private static final int maxInputAttempts = Main.config.getInt("max-auth-app-attempts");
    private static final ByteBuf kickInvalidCodeMessagePacket = OutDisconnectPacketConstructor.constAtPlay(Messages.get("google-auth-invalid-code"));

    private final VerifiedUser user;
    private final Vector3i loc;

    public VerifiedVirtualAuthBuilder(VerifiedUser user, Consumer<Boolean> onConfirm) {
        this(user, onConfirm, new int[1]);
    }

    //wrongAttempts: a single-element array rather than an instance field, since it must be captured by the
    //onConfirm wrapper passed to super() below - before "this" exists to hold a field on.
    private VerifiedVirtualAuthBuilder(VerifiedUser user, Consumer<Boolean> onConfirm, int[] wrongAttempts) {
        super(user, user.getData(), correct -> {
            if (!correct && ++wrongAttempts[0] >= maxInputAttempts) {
                MethodProvider.kickAsync(user, kickInvalidCodeMessagePacket);
                return;
            }
            onConfirm.accept(correct);
        }, false);
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
        user.writeConstSilently(accessConfirmedMessagePacket);

        send(Sounds.ENTITY_PLAYER_LEVELUP, user, vec3iLoc(user));
    }

    public static void visualsOnDeniedAccess(VerifiedUser user) {
        user.writeConstSilently(accessDeniedMessagePacket);

        send(Sounds.ENTITY_ITEM_BREAK, user, vec3iLoc(user));
    }

    public static Vector3i vec3iLoc(VerifiedUser user) {
        return SpigotConversionUtil.fromBukkitLocation(user.getPlayer().getLocation()).getPosition().toVector3i();
    }
}