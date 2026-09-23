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
                //The app genuinely isn't required YET - see the Spigot AuthDataChanges' equivalent for the
                //full reasoning (checking the token's existence here used to be wrong: since Alix 3.10.0,
                //PersistentUserData.saveToDatabase() eagerly generates+persists a token for EVERY account on
                //its very first save, so a token existing is no longer a useful signal at all - it's true for
                //virtually every account almost immediately, whether or not the app was ever enabled). The
                //CURRENT (pre-change) authSetting correctly covers both "brand new account" AND "an admin
                //'/as resetpassword' cleared AuthSettings without also invalidating the token" (unlike
                //'/as reset2fa').
                if (!requiresApp(params.getAuthSettings())) {
                    //SECURITY (found in ShadowOfHeaven's PR review, 2026-09-23): rotate the token HERE,
                    //right before showing it - see the Spigot AuthDataChanges' equivalent for the full
                    //reasoning (closes the same exposure as the "Show QR Code" button's own gate does for an
                    //already-enabled account, for the window BEFORE this account ever enables 2FA, WITHOUT
                    //making the token itself generally volatile the way rotating it on every ungated "show-qr"
                    //view would have - several other places, email encryption in particular, rely on it
                    //staying stable except through this class's/Reset Token's own transactional
                    //commitTokenAndEmail() writes). Nothing is armed yet at this point (confirmQRCodeThenRun()
                    //below hasn't run), so a failure here just propagates - there's no pending action to
                    //clean up.
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