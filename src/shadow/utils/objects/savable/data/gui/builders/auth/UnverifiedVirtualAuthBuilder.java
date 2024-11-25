package shadow.utils.objects.savable.data.gui.builders.auth;

import alix.common.messages.Messages;
import io.netty.buffer.ByteBuf;
import shadow.Main;
import shadow.utils.misc.methods.MethodProvider;
import shadow.utils.misc.packet.constructors.OutDisconnectPacketConstructor;
import shadow.utils.misc.packet.constructors.OutMessagePacketConstructor;
import shadow.utils.objects.savable.data.gui.PasswordGui;
import shadow.utils.users.types.UnverifiedUser;

public final class UnverifiedVirtualAuthBuilder extends VirtualAuthBuilder {

    private static final ByteBuf kickInvalidCodeMessagePacket = OutDisconnectPacketConstructor.constructConstAtPlayPhase(Messages.get("google-auth-invalid-code"));
    private static final ByteBuf invalidCodeMessagePacket = OutMessagePacketConstructor.constructConst(Messages.getWithPrefix("google-auth-invalid-code-chat-message"));

    private static final int maxInputAttempts = Main.config.getInt("max-auth-app-attempts");
    private final UnverifiedUser user;

    public UnverifiedVirtualAuthBuilder(UnverifiedUser user) {
        super(user, correct -> onCodeConfirm(user, correct), true);
        this.user = user;
    }

    private static void onCodeConfirm(UnverifiedUser user, Boolean correct) {
        if (!correct) {
            if (++user.authAppAttempts == maxInputAttempts) MethodProvider.kickAsync(user, kickInvalidCodeMessagePacket);
            else user.writeAndFlushConstSilently(invalidCodeMessagePacket);
            return;
        }
        user.logIn();
/*        switch (user.getData().getLoginParams().getAuthSettings()) {
            case AUTH:
                user.logIn();//just auth, log in,
                return;
            case AUTH_AND_PASSWORD:
                user.init2FAPassword0();
        }*/
    }

    @Override
    void playSoundOnSuccess() {
        this.user.writeAndFlushConstSilently(PasswordGui.playerLevelUpSoundPacket);
    }

    @Override
    void playSoundOnDenial() {
        this.user.writeAndFlushConstSilently(PasswordGui.villagerNoSoundPacket);
    }

    @Override
    void playSoundOnDigitAppend() {
        this.user.writeAndFlushConstSilently(PasswordGui.noteBlockHarpSoundPacket);
    }

    @Override
    void playSoundOnLastRemove() {
        this.user.writeAndFlushConstSilently(PasswordGui.noteBlockSnareSoundPacket);
    }

    @Override
    void playSoundOnAllReset() {
        this.user.writeAndFlushConstSilently(PasswordGui.itemBreakSoundPacket);
    }
}