package shadow.utils.objects.savable.data;

import alix.common.data.AuthSetting;
import alix.common.data.LoginParams;
import alix.common.messages.Messages;
import alix.common.packets.message.MessageWrapper;
import alix.common.utils.formatter.AlixFormatter;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import io.netty.buffer.ByteBuf;
import shadow.utils.misc.packet.constructors.OutMessagePacketConstructor;
import shadow.utils.objects.savable.data.gui.builders.auth.VerifiedVirtualAuthBuilder;
import shadow.utils.users.types.VerifiedUser;

import static alix.common.messages.Messages.getWithPrefix;

public final class AuthDataChanges {

    private static final ByteBuf appliedChangesMessagePacket = OutMessagePacketConstructor.constructConst(getWithPrefix("gui-google-auth-applied-changes"));
    private AuthSetting authSetting;

    public AuthDataChanges() {
    }

    public void tryApply(VerifiedUser user) {
        if (authSetting == null) return;
        LoginParams params = user.getData().getLoginParams();

        if (params.hasProvenAuthAccess()) {
            this.apply0(user);
            VerifiedVirtualAuthBuilder.send(Sounds.ENTITY_PLAYER_LEVELUP, user, VerifiedVirtualAuthBuilder.vec3iLoc(user));
            user.getPlayer().closeInventory();
            return;
        }
        switch (authSetting) {
            case AUTH_APP:
            case PASSWORD_AND_AUTH_APP:
                user.getDuplexProcessor().verifyAuthAccess(() -> this.apply0(user));
        }
    }

    private void apply0(VerifiedUser user) {
        LoginParams params = user.getData().getLoginParams();
        boolean wasRequired = requiresApp(params.getAuthSettings());

        user.writeAndFlushConstSilently(appliedChangesMessagePacket);
        //VerifiedVirtualAuthBuilder.
        params.setAuthSettings(authSetting);

        //The app is newly becoming required (it wasn't a moment ago) - generate this account's first set
        //of recovery codes right now, same as a "Reset Code" does, rather than leaving the player with
        //none until they happen to click "Recovery Codes" or "Reset Code" separately. RecoveryCodes.generate()
        //is cheap in-memory work and saveRecoveryCodes() is a fire-and-forget async DB write (see
        //DatabaseUpdaterImpl), so this is safe to do directly here without offloading to another thread.
        if (!wasRequired && requiresApp(this.authSetting)) {
            String[] codes = user.getData().regenerateRecoveryCodes();
            sendRecoveryCodes(user, codes);
        }
    }

    private static boolean requiresApp(AuthSetting setting) {
        return setting == AuthSetting.AUTH_APP || setting == AuthSetting.PASSWORD_AND_AUTH_APP;
    }

    private static void sendRecoveryCodes(VerifiedUser user, String[] codes) {
        var player = user.getPlayer();
        player.sendMessage(MessageWrapper.parseLegacy(Messages.getWithPrefix("gui-google-auth-recovery-codes-chat-header")));
        for (String code : codes) {
            player.sendMessage(MessageWrapper.parseLegacy(AlixFormatter.translateColors("&e" + code)));
        }
        player.sendMessage(MessageWrapper.parseLegacy(Messages.getWithPrefix("gui-google-auth-recovery-codes-chat-footer")));
    }

    public void setAuthSetting(AuthSetting authSetting) {
        this.authSetting = authSetting;
    }
}