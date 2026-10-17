package alix.velocity.systems.packets.gui.changes;

import alix.common.data.AuthSetting;
import alix.common.data.LoginParams;
import alix.common.messages.Messages;
import alix.common.utils.formatter.AlixFormatter;
import alix.velocity.utils.user.VerifiedUser;

import static alix.common.messages.Messages.getWithPrefix;

public final class AuthDataChanges {

    private static final String appliedChangesMessage = getWithPrefix("gui-google-auth-applied-changes");
    private AuthSetting authSetting;

    public AuthDataChanges() {
    }

    //showQrCode is passed in by the caller (GoogleAuthGUI, via this::showQrCode) rather than called
    //statically, since Velocity's QR-display logic lives as instance state on that GUI, unlike Spigot's
    //standalone GoogleAuth utility class.
    public void tryApply(VerifiedUser user, Runnable showQrCode) {
        if (authSetting == null) return;
        LoginParams params = user.getData().getLoginParams();

        if (params.hasProvenAuthAccess()) {
            this.apply0(user);
            //VerifiedVirtualAuthBuilder.send(Sounds.ENTITY_PLAYER_LEVELUP, user, VerifiedVirtualAuthBuilder.vec3iLoc(user));
            //user.getPlayer().closeInventory();
            user.closeInventory();
            return;
        }
        switch (authSetting) {
            case AUTH_APP:
            case PASSWORD_AND_AUTH_APP:
                //Checks the CURRENT authSetting, not token existence - since Alix 3.10.0 eagerly generates a
                //token for every account on first save, token existence alone no longer signals whether the
                //app was ever enabled. Covers both a brand-new account and an admin '/as resetpassword'
                //clearing AuthSettings without invalidating the token.
                if (!requiresApp(params.getAuthSettings())) {
                    //Rotates the token right before showing it, so anyone who saw an earlier, never-enabled
                    //QR code can't have it become real 2FA the moment this flow completes - without making
                    //the token generally volatile (other places, e.g. email encryption, rely on it staying
                    //stable otherwise). Nothing is armed yet (confirmQRCodeThenRun() below hasn't run), so a
                    //failure here just propagates.
                    user.getData().regenerateAuthToken();
                    //Show the QR code first - nothing sensitive to leak yet - then only actually apply the
                    //change once the player proves they scanned it correctly.
                    user.getDuplexProcessor().confirmQRCodeThenRun(() -> this.apply0(user));
                    try {
                        showQrCode.run();
                    } catch (RuntimeException e) {
                        //showQrCode() can fail before ever entering the QR-view state (e.g. QR image
                        //generation itself throwing) - endQRCodeShow()'s own cleanup only ever runs once
                        //that state is actually entered, so without this the pending action above would be
                        //left armed and could later fire on a completely unrelated QR confirmation (the
                        //plain, ungated "show-qr" button reuses this same method).
                        user.getDuplexProcessor().confirmQRCodeThenRun(null);
                        throw e;
                    }
                } else {
                    //The app is already required right now - showing its QR to an unproven session would
                    //leak the account's real, still-live 2FA factor. Require proof of the EXISTING code first.
                    user.getDuplexProcessor().verifyAuthAccess(() -> this.apply0(user));
                }
        }
    }

    private void apply0(VerifiedUser user) {
        LoginParams params = user.getData().getLoginParams();
        boolean wasRequired = requiresApp(params.getAuthSettings());

        user.user.sendMessage(appliedChangesMessage);
        //VerifiedVirtualAuthBuilder.
        params.setAuthSettings(authSetting);

        //The app is newly becoming required (it wasn't a moment ago) - generate this account's first set
        //of recovery codes right now, same as a "Reset Code" does, rather than leaving the player with
        //none until they happen to click "Recovery Codes" or "Reset Code" separately - see the Spigot
        //AuthDataChanges' equivalent for the full reasoning.
        if (!wasRequired && requiresApp(this.authSetting)) {
            //see the Spigot AuthDataChanges equivalent for why this must land first
            user.getData().persistToken();
            String[] codes = user.getData().regenerateRecoveryCodes();
            sendRecoveryCodes(user, codes);
        }
    }

    private static boolean requiresApp(AuthSetting setting) {
        return setting == AuthSetting.AUTH_APP || setting == AuthSetting.PASSWORD_AND_AUTH_APP;
    }

    private static void sendRecoveryCodes(VerifiedUser user, String[] codes) {
        user.user.sendMessage(Messages.getWithPrefix("gui-google-auth-recovery-codes-chat-header"));
        for (String code : codes) {
            user.user.sendMessage(AlixFormatter.translateColors("&e" + code));
        }
        user.user.sendMessage(Messages.getWithPrefix("gui-google-auth-recovery-codes-chat-footer"));
    }

    public void setAuthSetting(AuthSetting authSetting) {
        this.authSetting = authSetting;
    }
}