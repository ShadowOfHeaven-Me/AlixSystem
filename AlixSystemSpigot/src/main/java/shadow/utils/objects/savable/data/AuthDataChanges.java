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
                //The app genuinely isn't required YET (not just "no token row exists" - since Alix 3.10.0,
                //PersistentUserData.saveToDatabase() eagerly generates+persists a token for EVERY account on
                //its very first save, whether or not the app was ever enabled, so a token existing is no
                //longer a useful signal here at all - see GoogleAuthGUI#runGatedByAuthAccess() for the full
                //story of why this was wrong before this fix). Checking the CURRENT (pre-change)
                //authSetting instead correctly covers both "brand new account" AND "an admin '/as
                //resetpassword' cleared authSettings back to PASSWORD without touching the token/secret"
                //(unlike '/as reset2fa', which explicitly invalidates it) - either way, nothing has actually
                //been shown to the player yet for THIS requirement to exist, so there's nothing to prove.
                if (!requiresApp(params.getAuthSettings())) {
                    //SECURITY (found in ShadowOfHeaven's PR review, 2026-09-23): rotate the token HERE,
                    //right before showing it, rather than leaving the account's existing (possibly
                    //long-pre-generated, since Alix 3.10.0 provisions one for every account at creation -
                    //see the comment above) secret in place indefinitely. Two reasons:
                    //  1. It closes the same exposure the "Show QR Code" button's own gate closes for an
                    //     ALREADY-enabled account (proof-of-access before revealing the live secret) for the
                    //     window BEFORE this account ever enables 2FA too - anyone who saw this account's QR
                    //     earlier via that button (nothing gates it while the app isn't required, since
                    //     there's genuinely nothing live to protect yet) would otherwise still be holding a
                    //     secret that becomes real, active 2FA the moment this exact flow completes.
                    //  2. It does this WITHOUT making the token itself generally volatile - unlike an
                    //     earlier version of this fix, which rotated it on every ungated "Show QR Code" view
                    //     instead: several other places (email encryption in particular, see
                    //     PersistentUserData/DatabaseUpdaterImpl#readEmail()) rely on the token staying
                    //     stable except through this class's own commitTokenAndEmail()-backed, transactional
                    //     writes (regenerateAuthToken() here and in GoogleAuthGUI's "Reset Token") - rotating
                    //     it from a casual, unconfirmed button click risked exactly that assumption breaking
                    //     (e.g. a multi-server setup's local token cache going stale relative to what an
                    //     email was actually last encrypted under). Rotating only HERE, at the one moment
                    //     this specific secret is actually about to become live, is rare and deliberate -
                    //     the same shape as the pre-existing "Reset Token" action - not continuous churn.
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