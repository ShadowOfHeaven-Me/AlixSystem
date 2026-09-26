package alix.velocity.systems.packets.gui;

import alix.common.messages.Messages;
import alix.common.packets.message.MessageWrapper;
import alix.velocity.utils.user.VerifiedUser;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import io.netty.channel.Channel;
import ua.nanit.limbo.connection.login.gui.bedrock.AbstractAuthBuilder;
import ua.nanit.limbo.connection.login.packets.SoundPackets;
import ua.nanit.limbo.connection.pipeline.encryption.CipherHandler;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.packets.PacketUtils;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.protocol.snapshot.PacketSnapshot;

import java.util.function.Consumer;

public final class VelocityAuthBuilder extends AbstractAuthBuilder {

    //FUNCTIONALITY (audit, 2026-09-24): this gate (Reset Token / View Recovery Codes / Show QR Code -
    //"prove your CURRENT app code before we show/change anything sensitive") is explicitly meant to defend
    //against someone briefly at an unattended, already-logged-in session - unlike every other code-entry
    //path in the codebase, it had no attempt cap at all: a wrong guess just played a "denied" sound and let
    //the player try again immediately, unlimited times, against the live 6-digit code. See
    //VerifiedVirtualAuthBuilder's matching Spigot fix, which reuses the existing max-auth-app-attempts
    //config key - Velocity has no config plumbing for that key at all yet (Spigot-only today), so this uses
    //a fixed cap instead rather than adding new config schema as part of this fix.
    private static final int maxInputAttempts = 3;

    private final VerifiedUser user;

    //packet writing
    private final Channel channel;
    private final Version version;
    private final CipherHandler cipher;

    public VelocityAuthBuilder(VerifiedUser user, Consumer<Boolean> onConfirm, boolean includeLeaveButton) {
        super(user.getData(), wrapWithAttemptCap(user, onConfirm), includeLeaveButton);
        this.user = user;
        this.channel = user.getChannel();
        this.version = Version.of(user.user.getClientVersion().getProtocolVersion());
        this.cipher = CipherHandler.encryptionFor(this.channel);
    }

    //A static helper (rather than an instance field) so the counter can be captured by the onConfirm
    //wrapper passed to super() below - before "this" exists to hold a field on.
    private static Consumer<Boolean> wrapWithAttemptCap(VerifiedUser user, Consumer<Boolean> onConfirm) {
        int[] attempts = new int[1];
        return correct -> {
            if (!correct && ++attempts[0] >= maxInputAttempts) {
                user.getPlayer().disconnect(MessageWrapper.parseLegacy(Messages.get("google-auth-invalid-code")));
                return;
            }
            onConfirm.accept(correct);
        };
    }

    @Override
    public void onCloseAttempt() {
        this.user.getDuplexProcessor().endQRCodeShow();
    }

    private static final String
            accessConfirmedMessagePacket = Messages.getWithPrefix("google-auth-access-confirmed"),
            accessDeniedMessagePacket = Messages.getWithPrefix("google-auth-access-confirmation-failed");

    public static void visualsOnProvenAccess(VelocityAuthBuilder builder, VerifiedUser user) {
        builder.write(SoundPackets.PLAYER_LEVELUP);
        user.sendMessage(accessConfirmedMessagePacket);
    }

    public static void visualsOnDeniedAccess(VelocityAuthBuilder builder, VerifiedUser user) {
        builder.write(SoundPackets.ITEM_BREAK);
        user.sendMessage(accessDeniedMessagePacket);
    }

    @Override
    protected void write(PacketOut packet) {
        PacketUtils.write(this.channel, this.version, packet, this.cipher);
    }

    @Override
    protected void writeAndFlush(PacketOut packet) {
        PacketUtils.writeAndFlush(this.channel, this.version, packet, this.cipher);
    }

    @Override
    protected void sendPacketAndClose(PacketSnapshot packet) {
        PacketUtils.closeWith(this.channel, this.version, packet, this.cipher);
    }

    @Override
    protected ClientVersion getClientVersion() {
        return this.version.getClientVersion();
    }
}