package shadow.utils.objects.savable.data;

import alix.common.data.AuthSetting;
import alix.common.data.LoginParams;
import alix.common.messages.Messages;
import alix.common.packets.message.MessageWrapper;
import alix.common.utils.formatter.AlixFormatter;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import io.netty.buffer.ByteBuf;
import shadow.systems.login.auth.GoogleAuth;
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
                //Checks the CURRENT authSetting, not token existence - since Alix 3.10.0 eagerly generates a
                //token for every account on first save, token existence alone no longer signals whether the
                //app was ever enabled. Covers both a brand-new account and an admin '/as resetpassword'
                //clearing authSettings without invalidating the token (unlike '/as reset2fa').
                if (!requiresApp(params.getAuthSettings())) {
                    //Rotates the token right before showing it, so anyone who saw an earlier, never-enabled
                    //QR code (nothing gates viewing it while the app isn't required yet) can't have it
                    //become real 2FA the moment this flow completes - without making the token generally
                    //volatile, since other places (email encryption) rely on it staying stable except
                    //through this class's own transactional writes.
                    //Never invalidates a scan-in-progress: the player hasn't seen ANY QR for this specific
                    //authSetting change yet at this point, so there's nothing to invalidate - showQRCode()
                    //right below displays exactly this freshly (re)generated token, and the pending confirm
                    //action below checks against this same, now-current value. Nothing is armed yet at this
                    //point (confirmQRCodeThenRun() below hasn't run), so a failure here just propagates -
                    //there's no pending action to clean up.
                    user.getData().regenerateAuthToken();
                    //Show the QR code first - there's nothing sensitive to leak yet since the app isn't
                    //required - then only actually apply the change once the player proves they scanned it
                    //correctly (types "confirm" and enters the code their app just generated), rather than
                    //applying immediately and trusting blindly that they will get around to it.
                    user.getDuplexProcessor().confirmQRCodeThenRun(() -> this.apply0(user));
                    try {
                        GoogleAuth.showQRCode(user, user.getPlayer());
                    } catch (RuntimeException e) {
                        //showQRCode() can fail before ever entering the QR-view state (e.g. QR image
                        //generation itself throwing) - endQRCodeShow()'s own cleanup only ever runs once
                        //that state is actually entered, so without this the pending action above would be
                        //left armed and could later fire on a completely unrelated QR confirmation (the
                        //plain, ungated "Show QR Code" button reuses this same method).
                        user.getDuplexProcessor().confirmQRCodeThenRun(null);
                        throw e;
                    }
                } else {
                    //The app is already required right now (e.g. switching between AUTH_APP and
                    //PASSWORD_AND_AUTH_APP, or the resetpassword case above) - showing its QR to an unproven
                    //session would leak the account's real, still-live 2FA factor. Require proof of the
                    //EXISTING code first, same as "Reset Token" does.
                    user.getDuplexProcessor().verifyAuthAccess(() -> this.apply0(user));
                }
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